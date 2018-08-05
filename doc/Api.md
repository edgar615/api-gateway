# API

API采用JSON格式来定义上层接口与下游服务直接的转发规则。
Api定义支持三种风格的路径

- 标准匹配，用于精确定义某个API，method和path必须完全相同
- 正则表达式，用于精确定义某个API，method必须完全相同，path必须匹配正则表达式
- Ant风格，用于定义下游服务的一组API，method必须完全相同，path必须匹配ANT风格


网关在查找API定义的时候按照下面的优先级查找，如果匹配到合适的API，就不在继续查找Ant风格的API

```
标准匹配 > 正则表达式 > Ant风格
```

## 标准匹配
一个最简单的例子
```
{
  "name": "ping",
  "method": "GET",
  "path": "/ping",
  "endpoints": [
    {
      "name": "ping",
      "type": "dummy",
      "result" : {
      }
    }
  ]
}
```
- **name** API的名称，在同一个网关里，这个名称必须唯一。相同的名称会被覆盖，建议按照 `[业务线].[应用名].[动作].[版本]`的格式定义。
- **method** API的HTTP方法，仅支持GET、POST、PUT、DELETE
- **path** API的地址，支持正则表达式匹配
- **endpoints** 下游服务的转发规则定义，JSON数字，一个API可以向多个下游服务转发。**考虑到分布式事务问题，建议只有GET请求才可以向多个下游服务转发**。为了满足不同的转发规则，我们定义了几个不同类型的Endpoint，稍后会详细介绍。

 **注意：所有的下游服务的响应内容均要求是JSON格式**
  
### dummy
Dummy类型的Endpoint是最简单的endpoint，它不向下游服务转发请求，而是直接使用result作为返回。使用dummy，我们可以实现简单的ping-pong的健康检查功能。
dummy类型的endpoint只有三个属性

- **type** dummy
- **name** endpoint的名称，所有的endpoint都必须有名称，后面有一些插件需要依赖于这个名称实现。这个名称可以随意定义，只要在同一个API定义中唯一就可以。
- **result** dummy需要返回的JSON对象。目前不支持JSON数组

**注意：在对API定义的时候，允许有一个缺省的name：`default` 可以简化一部分工作**，因为其实大部分API都只会转发到一个endpoint.

### eventbus
eventbus类型的Endpoint使用vert.x的eventbus转发请求到下游服务。
配置示例：
```
{
  "name": "pub",
  "type": "eventbus",
  "policy" : "pub-sub",
  "address" : "event.user.keepalive"
}
```

**policy**  因为vert.x有三种类型的事件，所以我们也定义了三种不同的endpoint。通过policy区分pub-sub、point-point、req-resp。转发的事件内容都是HTTP请求的请求体，但是我们可以通过请求转换插件实现更多功能

对于eventbus的使用有一个需要注意的地方，eventbus并没有查询字符串`queryString`的概念，所以如果API调用没有请求体，向下游的eventbus的转发的事件就是一个空的JSON对象，此时需要我们使用转换插件将queryString或者header加入到body中。

#### pub-sub
使用publish向所有订阅方广播事件，它不需要关注是否存在订阅方或者订阅方有无收到消息。所以这个类型的请求每次都是成功。
返回结果
```
{"result":1}
```
我们可以通过响应转换插件对响应结果做更多的操作。

#### point-point
使用send向一个订阅方发送事件，它也不需要关注是否存在订阅方或者订阅方有无收到消息。所以这个类型的请求每次都是成功。
返回结果
```
{"result":1}
```

#### req-resp
使用send向一个订阅方发送事件，并等待回应。如果订阅方不存在，请求会返回失败。
订阅方不存在的结果
```
{"message":"Service Unavailable","details":"No handlers","code":1016}
```
对于req-resp类型的事件，因为需要等待订阅方的返回，所以需要有超时设置（默认为30秒）。
为简单的实现自定义功能，通过请求转换功能，在请求头中添加下面的请求头
```
"x-delivery-timeout" : "30"
```

### simple-http
向下游服务发起REST请求的Endpoint，在每个endpoint里需要配置下游服务的IP和端口。
配置示例
```
{
  "name": "health",
  "type": "simple-http",
  "path": "/health-check",
  "host" : "127.0.0.1",
  "port" : 10000
}
```
- **path** 下游服务的接口地址
- **host** 下游服务的IP
- **port** 下游服务的端口

### http
使用服务发现机制，搜索下游服务，然后再向下游服务发起REST请求的Endpoint。这是我们在网关里最常用的一个Endpoint。
配置示例
```
{
  "name": "devices",
  "type": "http",
  "path": "/health-check",
  "service" : "device"
}
```
- **path** 下游服务的接口地址
- **service** 下游服务的服务名称
  **使用这个endpoint需要配合XXXServiceDiscoveryVerticle才能实现**
  
## 正则风格的地址匹配
使用标准匹配无法满足GET /user/:id 这种风格的API，所以需要使用正则来匹配
  
  示例
  ```
  {
    "name": "user-regex",
    "method": "GET",
    "path": "/user/([\d+]+)",
    "type" : "regex",
    "endpoints": [
      {
        "type": "http",
        "path": "/v1/$var.param0",
        "service" : "user"
      }
    ]
  }
  ```
正则表达式所对应的值转换为对应的参数.
参数名为param0  0表示匹配的第0个字符串，从0开始计算；
参数值为正则表达式在请求路径中的值.
所有的参数名将保存在上下文变量中，可以通过$var.param0变量来获得
  
## Ant风格的地址匹配
有时候如果我们针对API一个个配置匹配
规则如下：
```
？匹配一个字符
*匹配0个或多个字符
**匹配0个或多个目录
```
示例
```
{
  "name": "user-ant",
  "method": "GET",
  "path": "/v1/user/**",
  "type" : "ant",
  "endpoints": [
    {
      "name": "user-ant",
      "type": "http",
      "path": "/v1/$var.extractPath",
      "service" : "user"
    }
  ]
}
```
## 扩展
前面API定义章节已经描述了如何使用最核心的的转发功能，但是在实际业务中API网关还需要承载更多的功能，如鉴权、参数校验、限流等等，我们通过Plugin和Filter两个组件组合使用来实现各种不同的需求，只有Plugin和Filter组合起来才能发挥API网关的最大威力。

Plugin用于扩展API的功能，Filter则是这些扩展的具体实现。

Filter分为两种PRE和POST

- PRE 在向下游服务转发请求前执行，大多数的filter都是PRE类型的Filter
- POST 在收到下游服务响应后执行

每个Filter内部都有一个order，API网关在收到请求之后会按顺序执行Filter。
