# direwolves
API网关,准备造的一个轮子

使用Vert.x实现

## Verticle


## API定义

### API路由匹配
当API网关收到调用方的请求后，首先需要通过api-discovery模块根据请求方法和请求地址匹配到对应的API定义才能继续处理请求。如果没有找到对应的API定义，会直接返回404。
我定义了两种API匹配方式：ApiMatchFilter和GrayFilter
#### Filter: ApiMatchFilter
默认使用的API查找。如果未找到对应的API，返回404（资源不存在），如果根据正则匹配到多个API，返回500（数据冲突）。

- **type** PRE
- **order** -2147482648 

**前置条件**：上下文中不存在API

**多版本匹配**
随着业务的不断发展，需求变更，APP迭代越来越频繁，过老的版本不会强制更新，因此API接口避免不了出现多个版本的情况。为了解决多版本匹配的问题，采用下面的策略

1. 调用方发送服务端请求是加上版本号的请求头，如：
```
x-api-verson : 20171108
```
2. 不同版本的API使用VersionPlugin定义版本
3. 定义一个带有ClientApiVersionPlugin插件的API。（可以重新定义一个新的API，也可以在原有的API上定义，推荐前者）
#### Plugin: VersionPlugin
用于定义API版本的插件，建议直接用日期来表示版本（GrayFilter并没有实现复杂的版本比较）
配置
```
"version": "20171108"
```
- **version**，对应版本号，使用日期格式。

**注意：多版本共存是API的名称不能重复**
#### Plugin: ClientApiVersionPlugin
用来声明基于请求头的灰度发布规则
配置
```
"version.match": "floor"
```
**version.match**用来指明在未匹配到`x-api-version`声明的版本时时，采用哪种方式匹配API。
- **floor** 匹配最低的版本
- **ceil** 匹配最高的版本
#### Filter: ClientApiVersionFilter

如果请求头中带有`x-api-version`则执行这个Filter。 如果未找到合适的API，返回404（资源不存在），如果匹配到多个API，返回500（数据冲突）。

- **type** PRE
- **order** -2147482748

**前置条件**：上下文中不存在API

在引入了plugin-gray包之后ClientApiVersionFilter会在ApiMatchFilter之前执行，如果通过ClientApiVersionFilter找到了合适的API，那么ApiMatchFilter不会再执行。

根据x-api-version我们可以实现一些简易的灰度发布规则，后面灰度发布部分会详细描述

#### Filter: PathParamFilter
将API定义中的正则表达式与请求路径做匹配，然后将正则表达式所对应的值转换为对应的参数.
参数名为param0  0表示匹配的第0个字符串，从0开始计算；参数值为正则表达式在请求路径中的值.
所有的参数名将保存在上下文变量中，可以通过$var.param0变量来获得

- **type** PRE
- **order** -2147481648

**前置条件**：使用正则表达式匹配的API

示例：
```
"path": "/regex/([\\d+]+)/test/([\\w+]+)",
```
用户请求 `/regex/95624/test/hlu6duKrlM`经过`PathParamFilter`解析之后会在上下文中保存两个变量
```
$var.param0:"95624"
$var.param1:"hlu6duKrlM"
```
#### Filter: AntPathParamFilter
将匹配ant风格的路径保存在上下文变量中，可以通过$var.extractPath变量来获得

- **type** PRE
- **order** -2147481648

**前置条件**：使用Ant匹配的API

示例：
```
"path": "/ant/**",
```
用户请求 `/ant/95624/test/hlu6duKrlM`经过`AntPathParamFilter`解析之后会在上下文中保存两个变量
```
$var.extractPath:"95624/test/hlu6duKrlM"
```

### 请求/响应转换
#### Plugin: RequestTransformerPlugin
有时候下游服务需要的参数与API网关公布出去的参数并不相同，此时我们可以通过RequestTransformerPlugin将请求参数按照一定的规则转换之后提供给下游服务。
配置
```
  "request.transformer": [
    {
      "name": "alarm_list",
      "header.add": [
        "x-auth-userId:$user.userId",
        "x-auth-companyCode:$user.companyCode",
        "x-policy-owner:individual"
      ],
      "header.remove": [
        "Authorization"
      ],
      "header.replace": [
        "x-app-verion:x-client-version"
      ],
      "query.add": [
        "userId:$user.userId"
      ],
      "query.remove": [
        "appKey",
        "nonce"
      ],
      "query.replace": [
        "x-app-verion:x-client-version"
      ],
      "body.add": [
        "userId:$user.userId"
      ],
      "body.remove": [
        "appKey",
        "nonce"
      ],
      "body.replace": [
        "x-app-verion:x-client-version"
      ]
    }
  ]
```
`request.transformer`使用一个JSON数组来保存每个下游服务的转换规则，数组中每个JSO你对象的属性如下

- **name** endpoint的名称，必填项，只有这个名称的endpoint才会执行参数转换
- **header.remove** 数组，需要删除的请求头
- **query.remove** 数组，需要删除的请求参数
- **body.remove** 数组，需要删除的请求体
- **header.replace** 数组，需要重命名的请求头，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- **query.replace** 数组，需要重命名的请求参数，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- **body.replace** 数组，需要重命名的请求体，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- **header.add** 数组，需要增加的请求头，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
- **query.add** 数组，需要增加的请求参数，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
- **body.add** 数组，需要增加的请求体，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值

**上述的转换的值支持使用$变量来表示，$变量最终会经过ReplaceFilter进行填充**
#### Filter: HttpRequestTransformerFilter
对HTTP类型的请求进行转换。

- **type** PRE
- **order** 15000

**前置条件**：
- 转发的请求中有HTTP类型的请求
- 配置中有`request.transformer`的全局参数或者api定义了RequestTransformerPlugin
  配置
```
 "request.transformer": {
   "header.add": [
	 "x-auth-userId:$user.userId",
	 "x-auth-companyCode:$user.companyCode",
	 "x-policy-owner:individual"
   ],
   "header.remove": [
	 "Authorization"
   ],
   "header.replace": [
	 "x-app-verion:x-client-version"
   ],
   "query.add": [
	 "userId:$user.userId"
   ],
   "query.remove": [
	 "appKey",
	 "nonce"
   ],
   "query.replace": [
	 "x-app-verion:x-client-version"
   ],
   "body.add": [
	 "userId:$user.userId"
   ],
   "body.remove": [
	 "appKey",
	 "nonce"
   ],
   "body.replace": [
	 "x-app-verion:x-client-version"
   ]
 }

```
配置示例与RequestTransformerPlugin的类似。但是全局配置针对所有的转发请求都有效，所以它直接使用一个JSON对象保存转换规则。
**先执行全局规则的转换，在执行插件的转换**
转换规则的执行顺序为：remove replace add
#### Filter EventbusRequestTransformerFilter
对Eventbus类型的请求进行转换。于RequestTransformerFilter类似。
**注意：**eventbus并没有queryString的概念，所以如果API调用没有请求体，向下游的eventbus的转发的事件就是一个空的JSON对象，此时需要我们使用转换插件将queryString或者header加入到body中。
#### Filter: HttpRequestReplaceFilter
用于将请求参数中带变量的参数用变量的实际值替换，一般与`request.transformer`结合使用

对于params和headers，如果新值是集合或者数组，将集合或数组的元素一个个放入params或headers，而不是将一个集合直接放入.(不考虑嵌套的集合),
例如：`q1 : $header.h1`对应的值是`[h1.1, h1.2]`，那么最终替换之后的新值是 `q1 : [h1.1,h1.2]`而不是 `q1 : [[h1.1,h1.2]]`

- type PRE
- order 2147482647.

**前置条件**： 转发的请求中有HTTP类型的请求

http类型的请求支持在请求路径中使用变量，如`/users/$user.userId`

一个例子
```
"request.transformer": [
  {
	"name": "add_cateye",
	"body.add": [
	  "userId:$user.userId",
	  "username:$user.username",
	  "companyCode:$user.companyCode"
	],
	"header.add": [
	  "x-auth-userId:$user.userId",
	  "x-auth-companyCode:$user.companyCode",
	  "x-policy-owner:individual"
	]
  }
]
```
经过HttpRequestReplaceFilter之后，body中的元素包括
```
    "userId":1,
    "username":"edgar",
    "companyCode":0
```
header中的元素包括
```
    "x-auth-userId":"1",
    "x-auth-companyCode":"0",
    "x-policy-owner" : "individual"
```
#### Filter: EventbusRequestReplaceFilter
于HttpRequestReplaceFilter类似

**前置条件**： 转发的请求中有HTTP类型的请求

**注意：**EventbusRequestReplaceFilter不支持对eventbus的地址做转换，因为对于eventbus来说每个事件地址应该是固定的。

#### Plugin: ResponseTransformerPlugin
与请求转换类型，将响应的结果按照一定的规则做转换.

配置示例：
```
"response.transformer": {
  "header.add": [
	"x-auth-userId:$user.userId",
	"x-auth-companyCode:$user.companyCode",
	"x-policy-owner:individual"
  ],
  "header.remove": [
	"Authorization"
  ],
  "header.replace": [
	"x-app-verion:x-client-version"
  ],
  "body.add": [
	"userId:$user.userId"
  ],
  "body.remove": [
	"appKey",
	"nonce"
  ],
  "body.replace": [
	"x-app-verion:x-client-version"
  ]
}
```
响应的转换规则并没有query.xxx规则，而且响应转换是将所有的响应合并后才执行的，所以不需要为每个转发定义转换规则。**目前我们直接将下游服务的响应头丢弃了，所以header的转换规则不需要考虑下游服务的响应头**


- **header.remove** 数组，需要删除的响应头
- **body.remove** 数组，需要删除的响应体
- **header.replace** 数组，需要重命名的响应头，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- **body.replace** 数组，需要重命名的响应体，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- **header.add** 数组，需要增加的响应头，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
- **body.add** 数组，需要增加的响应体，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值

#### Filter ResponseTransformerFilter
- **type** POST
- **order** 10000

**前置条件**：配置中有`response.transformer`或者API有ResponseTransformerPlugin

配置
```
  "response.transformer": {
    "header.add": [
      "x-auth-userId:$user.userId",
      "x-auth-companyCode:$user.companyCode",
      "x-policy-owner:individual"
    ],
    "header.remove": [
      "Authorization"
    ],
    "header.replace": [
      "x-app-verion:x-client-version"
    ],
    "body.add": [
      "userId:$user.userId"
    ],
    "body.remove": [
      "appKey",
      "nonce"
    ],
    "body.replace": [
      "x-app-verion:x-client-version"
    ]
  }
```
配置示例与ResponseTransformerPlugin的类似。
**先执行全局规则的转换，在执行插件的转换**
转换规则的执行顺序为：remove replace add

#### Filter: ResponseReplaceFilter
对响应结中的变量进行替换，一般与`response.transformer`结合使用。与RequestReplaceFilter类似。
暂时不支持JSON数组类型的响应体

- type POST
- order 2147482647

**前置条件**： 所有请求

### RPC调用

#### Filter RpcFilter

- type PRE
- order int的最大值（用于在PRE的最后执行）

#### 断路器

使用vert.x提供的断路器组件实现了简单的断路器
配置参数

```
 "circuit.breaker" : {
   "maxFailures" : 5,
   "maxRetries" : 0,
   "resetTimeout" : 60000,
   "timeout" : 3000,
   "metricsRollingWindow" : 10000,
   "notificationPeriod" : 2000,
   "notificationAddress" : "vertx.circuit-breaker",
   "registry" : "vertx.circuit.breaker.registry",
    "cacheExpires": 3600
 }

```

- maxFailures  针对一个服务的请求失败多少次之后开启断路器，默认值5
- maxRetries 请求失败后的重试次数，默认值0
- resetTimeout 断路器打开之后，等待多长时间重置为半开状态，单位毫秒，默认值30000
- timeout 一个请求多长时间没有返回任务超时（失败）， 单位毫秒，默认值10000
- metricsRollingWindow 度量的时间窗口 单位毫秒，默认值10000
- notificationPeriod  通知周期，单位毫秒，默认值2000
- notificationAddress  通知地址，默认值vertx.circuit-breaker
- registry localmap中保存断路器的键值，默认值vertx.circuit.breaker.registry
- cacheExpires 每个服务节点的断路器状态的缓存失效时间，默认3600秒

断路器目前仅支持HTTP类型的RPC请求，如果对扩展的RPC需要实现断路器功能，需要实现CircuitBreakerExecutable接口

#### 降级: FallbackPlugin
与请求转换类型，将响应的结果按照一定的规则做转换.

配置示例：
```
  "request.fallback": {
    "add_device": {
      "statusCode" : 200,
      "result" : {
        "foo": "bar"
      }
    },
    "device.list" : {
      "statusCode" : 200,
      "result" : []
    }
  }
```
request.fallback通过JSONOBJECT保存RPC请求的降级结果，当RPC请求失败的时候会返回降级插件中配置的结果,JsonObject的键对应着endpoint的名称.它的值是包含两个属性的JSON对象

- statusCode 响应码
- result 返回结果，可以是JSON对象或者数组

#### Filter RequestFallbackFilter
- **type** POST
- **order** 14000

### 限流

### 负载均衡

## 缓存
后续更新
## CMD
后续更新

plugin.scope.add 设置ScopePlugin
参数 scope：权限值
plugin.scope.delete 删除ScopePlugin

## Metric
```
-Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=my-registry
```
后续更新
## 日志
后续更新

ServiceNonExistent:服务不存在
QueryStringInvalid: 查询字符串非法
BodyInvalid:body非法
ArgProhibited: 禁止的参数
FallbackExecuted:降级执行
UserForbidden: 用户被拒绝访问
IpForbidden: 用户的IP被拒绝访问
AclForbidden: 用户的组被拒绝访问
UserPermissionAdmitted：用户鉴权通过
UserPermissionDenied：用户鉴权未通过
ClientPermissionAdmitted：客户端鉴权通过
ClientPermissionDenied：客户端鉴权未通过

BreakerHalfOpen 断路器半开
BreakerClose 断路器关闭
BreakerOpen 断路器打开

## 灰度发布

**灰度发布**
随着业务的不断发展，需求变更，接口迭代越来越频繁，为了降低全线升级引起的潜在危害，我们一般会采用灰度升级的方案，将部分请求转发到新接口上，然后再根据用户的反馈及时完善相关功能。
我们可以在两个地方来实现灰度升级的需求：

1. 在Nginx层做灰度规则判断，
2. 在网关层做灰度规则判断
   考虑到这种需求，我实现了一个很简单的基于请求头进行灰度规则的逻辑。这个灰度方案要求三处改动才能实现：
3. 调用方发送服务端请求是加上版本号的请求头，如：

```
x-api-verson : 20171108
```

1. 不同版本的API使用VersionPlugin定义版本
2. 定义一个带有ClientApiVersionPlugin插件的API。（可以重新定义一个新的API，也可以在原有的API上定义，推荐前者）

## 基准测试

后续更新
## 打包
单机模式安装standalone处理即可,**但是集群模式目前还未找到更好的方法**
在整个开发编译过程中都不需要依赖hazelcast和logback组件，但是在在集群部署时依赖hazelcast，所以在打包的时候需要加入hazelcast，找到了三种方式：

1. 将hazelcast引入依赖（不喜欢）
2. 通过maven-jar-plugin增加Class-Path，然后通过`java -jar`启动
```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-jar-plugin</artifactId>
	<configuration>
		<archive>
			<manifest>
				<addClasspath>true</addClasspath>
				<classpathPrefix>lib/</classpathPrefix>
				<mainClass>io.vertx.core.Launcher</mainClass>
			</manifest>
			<manifestEntries>
				<Class-Path>ext/hazelcast-3.6.3.jar ext/vertx-hazelcast-3.4.2.jar ext/logback-core-1.1.2.jar ext/logback-classic-1.1.2.jar</Class-Path>
				<Main-Verticle>${main.verticle}</Main-Verticle>
			</manifestEntries>
		</archive>
	</configuration>
</plugin>
```
3. 通过-cp指定classpath，**java -jar会忽略-cp**，所以我们只能通过-cp来运行Main方法
```
java -cp "./*;ext/*;lib/*" io.vertx.core.Launcher run ServiceDiscoveryVerticle --cluster
```
**windows用;分隔,linux用:分隔**
## TODO

- 监控
- 后台
- 对于GET请求，对于相同的请求可以做缓存、节流（throttleFirst，throttleLast）：在一个时间窗口内，如果有重复的请求正在处理，合并减少向后端服务发送请
- 所有的全局插件配置均可以动态修改(配置管理)

***************************************************************************
**华丽的分割线**
**下面的文字是很早零零散散写的，比较凌乱**
***************************************************************************




# 校验调用方的时间
## Filter: TimeoutFilter
调用方在QueryString中必须包含下列参数

    timestamp	时间戳	int	是	unix时间戳

如果参数不全或者客户端时间与服务端时间相差太久，服务端会认为是非法请求，返回1023的错误。

- type PRE
- order 6000

全局参数

     "timeout" : {
        "enable" : true, //是否启用filter，默认值true
        "expires": 300 //系统允许客户端或服务端之间的时间误差，单位秒，默认值300
     }


# 负载均衡

配置

    "load.balance": {
      "strategy": { //定义每个节点的负载均衡策略
        "user": "random",
        "device": "round_robin"
      }
    }

目前提供了下列负载均衡策略：

- random 随机选择
- round_robin 轮询
- weight_round_robin 基于权重的轮询
- sticky 只要服务节点可用，永远选择这个节点
- last_conn 最少连接数

如果某个服务没有配置负载均衡策略，默认使用轮询
