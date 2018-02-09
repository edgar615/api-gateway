# direwolves
API网关,准备造的一个轮子

使用Vert.x实现

## Verticle
目前定义了一些Verticle：

- **MainVerticle** 启动多个Verticle的工具类
- **JsonServiceDiscoveryVerticle** 从配置文件中读取服务，并注册到ServiceDiscovery
- **ConsulServiceDiscoveryVerticle**  直接从Consul中读取服务，并注册到ServiceDiscovery
- **ZookeeperServiceDiscoveryVerticle** 直接从Zookeeper中读取服务，并注册到ServiceDiscovery
- **ApiDefinitionVerticle** 提供对API在线操作的工具
- **FileApiDiscoveryVerticle** 从文件中读取API定义
- **RedisVerticle** 创建RedisClient
- **ApiDispatchVerticle** rest服务
- **ApiGitVerticle** 从GitHub pull路由定义文件到本地文件夹，它应该依赖于FileApiDiscoveryVerticle

其中XXXServiceDiscoveryVerticle、ApiDefinitionVerticle、ApiDispatchVerticle可以使用集群模式分开独立部署。也可以使用MainVerticle作为一个单节点应用部署

### MainVerticle
MainVerticle是我实现的一个工具类——用于启动有多个Verticle的Vert.x应用。这个工具类的灵感来自https://github.com/groupon/vertx-utils 。

1.在打包时需要通过maven-jar-plugin指定JAR的`Main-Class`为`io.vertx.core.Launcher`，同时指定`Launcher`的`Main-Verticle`为`com.github.edgar615.util.vertx.deployment.MainVerticle`。PS:使用其他的打包工具也可以。下面是一个完整示例
```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-jar-plugin</artifactId>
	<version>2.6</version>
	<configuration>
		<archive>
			<manifest>
				<addClasspath>true</addClasspath>
				<classpathPrefix>lib/</classpathPrefix>
				<mainClass>io.vertx.core.Launcher</mainClass>
			</manifest>
			<manifestEntries>
				<Main-Verticle>com.github.edgar615.util.vertx.deployment.MainVerticle</Main-Verticle>
			</manifestEntries>
		</archive>
	</configuration>
</plugin>
```
2. 启动MainVerticle

  java -jar XXX.jar --conf=config.json

3. config.json的配置说明
  一个简单的例子：
```
{
  "verticles": {
    "ApiDefinitionVerticle": {
      "class": "com.github.edgar615.direwolves.verticle.ApiDefinitionVerticle",
      "instances": 1,
      "worker": false,
      "config": {
        
      }
    },
    "ApiDispatchVerticle": {
      "class": "com.github.edgar615.direwolves.verticle.ApiDispatchVerticle",
      "instances": 1,
      "worker": false,
      "dependencies": [
      ],
      "config": {
        
      }
    }
  }
}
```
verticles是config.json中必须包含的JSON对象，这个JSON对象的属性名就是需要启动的Verticle的名字。我们通过ApiDispatchVerticle来看下启动一个Verticle需要配置配置属性
```
"ApiDispatchVerticle": {
      "class": "com.github.edgar615.direwolves.verticle.ApiDispatchVerticle",
      "instances": 1,
      "worker": false,
      "dependencies": [
      ],
      "config": {
        
      }
    }
  }
```
- **ApiDispatchVerticle** Verticle的名称，在其他Verticle需要依赖这个Verticle的时候会被用到
- **class** Verticle的class名称，必填项
- **instances** 需要启动的示例数，默认为1
- **worker** 是否使用工作线程启动，默认为false
- **dependencies** 依赖的Verticle，JSON数组，数组的元素就是需要依赖的Verticle名称。一旦配置了依赖关系，这个Verticle就会在依赖的Verticle全部启动完之后才启动
- **config** Verticle的配置项

一个依赖的例子：
```
{
  "verticles": {
    "RedisVerticle": {
      "class": "com.github.edgar615.direwolves.redis.RedisVerticle",
      "instances": 1,
      "worker": false,
      "config": {

      }
    },
    "ApiDispatchVerticle": {
      "class": "com.github.edgar615.direwolves.verticle.ApiDispatchVerticle",
      "instances": 1,
      "worker": false,
      "dependencies": [
        "RedisVerticle"
      ],
      "config": {
        
      }
    }
  }
}
```
### JsonServiceDiscoveryVerticle 
在一些很小的应用中，并不需要consul、zookeeper这种动态的服务注册和发现机制。未了满足这种需求，可以将服务的映射地址写在JsonServiceDiscoveryVerticle的配置文件，由JsonServiceDiscoveryVerticle启动时读取，并最终注册到ServiceDiscovery。
配置示例：
```
{
  "service.discovery": {
    "announceAddress": "vertx.discovery.announce",
    "usageAddress": "vertx.discovery.usage",
    "name": "service-discovery"
  },
  "services": {
    "user": [
      {
        "host": "192.168.0.100",
        "port": 8080
      },
      {
        "host": "192.168.0.101",
        "port": 8080
      }
    ],
    "device": [
      {
        "host": "192.168.0.100",
        "port": 8081
      }
    ]
  }
}
```
#### service.discovery
service.discovery配置是vert.x提供的service-discovery组件的配置，我们只需要关注三个属性即可
- **announceAddress**: 服务注册和卸载之后的广播地址，默认为vertx.discovery.announce
- **usageAddress**: 服务被使用后的广播地址，默认为vertx.discovery.usage
- **name**: 服务发现模块的名称，service-discovery组件会使用这个名称在vert.x的共享数据中存储服务信息。

在后面ApiDispatchVerticle的文档中我们还会发现同样的service.discovery配置，同一个网关的这个配置需要保持一致。否则会导致服务发现模块不能正常工作。
#### services
服务的地址映射。services是一个JSON对象，它的属性名就是各个服务的名称，属性值是一个保存了各个服务节点信息的JSON数组。目前节点信息仅使用了`host`和`port`两个属性，如果后面有增加的属性再补充
### ConsulServiceDiscoveryVerticle
使用consul作为服务发现的底层。通过vert.x提供的vertx-service-discovery-bridge-consul组件定时读取consul中的服务信息并刷新到service-discovery
配置示例
```
{
  "service.discovery": {
    "announceAddress": "vertx.discovery.announce",
    "usageAddress": "vertx.discovery.usage",
    "name": "service-discovery"
  },
  "consul": {
    "host": "localhost",
    "port": 8500,
    "scan-period": 2000
  }
}
```
#### service.discovery
与JsonServiceDiscoveryVerticle相同
#### consul
保存了vertx-service-discovery-bridge-consul需要的配置信息
- **host**: consul的地址
- **port**: consul的端口
- **scan-period**: 读取consul的时间间隔，单位毫秒

### ZookeeperServiceDiscoveryVerticle
使用zookeeper作为服务发现的底层。监听zookeeper中的服务信息的变化并刷新到service-discovery。
在实现这个模块的时候vert.x官方提供的zookeeper组件还有些BUG(不清楚现在有没有修复)，所以这部分是我参考consul自己实现的。**由于公司一些遗留项目的服务注册playload用的String，这里也是使用string来定义的playload，在未来会修改这个实现**
配置示例
```
{
  "service.discovery": {
    "announceAddress": "vertx.discovery.announce",
    "usageAddress": "vertx.discovery.usage",
    "name": "service-discovery"
  },
  "zookeeper": {
    "connect": "localhost:2181",
    "path": "/micro-service",
    "retry.sleep": 1000,
    "retry.times": 3
  }
}
```
#### service.discovery
与JsonServiceDiscoveryVerticle相同
#### zookeeper
保存了zookeeper需要的配置信息
- **connect**: zookeeper的地址和断开
- **path**: 服务注册的根路径
- **retry.sleep**: 重试间隔，单位毫秒
- **retry.times**: 重试次数

### FileApiDiscoveryVerticle
在启动时从文件中读取API定义，因为这个Verticle需要读取很多的文件，建议采用work模式启动
配置示例
```
{
  "api.discovery" : {
    "name" : "iotp-app",
    "publishedAddress" : "direwolves.api.published",
    "unpublishedAddress" : "direwolves.api.unpublished"
  },
  "path" : "H:/csst/java-core/trunk/06SRC/iotp-app/router/api/backend",
  "watch" : true
}
```
#### path
API定义存放的路径
#### watch
是否监控path目录下文件的变化，如果开启，文件的任何变化都会引起对应ApiDiscovery的重新加载
.**因为API的名称是写在文件中的，所以文件变化的时候，并不知道是变化的是哪个API，除非强制API名称就是文件名**

FileApiDiscoveryVerticle订阅`api.discovery.reload.<网关名>`事件，在接收到这个事件后会重新加载ApiDiscovery
文件监听功能就是通过发送这个事件来实现的API刷新，我们也可以配置一个API路由来实现手动刷新
```
{
  "name": "api.reload.1.0.0",
  "method": "GET",
  "path": "/api/reload",
  "scope": "api:mgr",
  "endpoints": [
    {
      "policy": "point-point",
      "name": "reload.api",
      "type": "eventbus",
      "address" : "api.discovery.reload.example"
    }
  ]
}
```

**如果开启了ApiGitVerticle，不需要开启watch功能，因为git的每次pull操作都会发送刷新事件**
### ApiGitVerticle
在启动时从GitHub中加载路由定义文件，并通知FileApiDiscoveryVerticle刷新路由。它应该在FileApiDiscoveryVerticle之后启动，这样才能避免FileApiDiscoveryVerticle丢失刷新事件。建议采用work模式启动
配置示例
```
{
  "url": "https://github.com/edgar615/config-test.git",
  "branch" : "master",
  "remote" : "origin",
  "path" : "H:/api",
  "name" : "example"
}
```
- url GitHub存放API定义的地址
- branch 分支 默认master
- remote 远程地址 默认origin
- path 本地clone的目录
- name API网关的名称

ApiGitVerticle订阅`api.discovery.git.<网关名>`事件，在接收到这个事件后会自动从GitHub上pull数据，然后通知FileApiDiscoveryVerticle刷新路由，所以我们也可以配置一个API路由来实现手动pull
```
{
  "name": "api.git.1.0.0",
  "method": "GET",
  "path": "/api/git",
  "scope": "api:mgr",
  "endpoints": [
    {
      "policy": "point-point",
      "name": "git.api",
      "type": "eventbus",
      "address" : "api.discovery.git.example"
    }
  ]
}
```
通过这个功能，我们可以通过GitHub的钩子，在push之后手动触发刷新功能

###  api.discovery
API发现组件的配置属性
- **publishedAddress**: 发布一个API后的广播地址
- **unpublishedAddress**: 删除一个API后的广播地址
- **name**: API发现模块的名称，api-discovery组件会使用这个名称在vert.x的共享数据中存储API信息。

**如果在一个应用里支持多个API网关，可以定义多个FileApiDiscoveryVerticle，但是需要注意不同业务的网关name、publishedAddress、unpublishedAddress三个属性不能相同，不然会导致API定义错乱**

### ApiDefinitionVerticle
定义了一些在线修改API定义的接口。后面详细
配置示例（暂时未定义配置）
```
{

}
```

### RedisVerticle
创建一个RedisClient。这个RedisClient是一个共享对象，同一个应用里直接可以用这个对象。
配置示例:
```
{
  "redis" : {
    "host": "localhost",
    "port": 6379,
    "auth": ""
  }
}
```
#### redis
redis属性用于定义RedisOptions中定义的属性
- **host** redis的地址
- **port** redis的端口
- **auth** redis的密码

### ApiDispatchVerticle
网关对外提供的REST服务。这个Verticle是整个网关的核心部分。会在后面详细介绍。
如果系统使用了redis作为缓存，那么ApiDispatchVerticle的依赖中需要加上RedisVerticle
#### 基础设置
```
 "namespace": "example", 网关的名称，默认为api-gatewawe
 "port": 9000, 网关的端口，默认为9000
 "bodyLimit": 1024, 请求体的body限制，-1为不限制，默认为不限制
```
#### CORS配置
网关默认不支持跨域访问，如果需要支持跨域，可以通过CORS配置开启
```
  "cors": {
    "allowedOriginPattern": "*.edgar615.com",
    "maxAgeSeconds": 1000,
    "allowedHeaders": ["*"],
    "allowedMethods": ["GET", "PUT", "POST", "DELETE", "OPTIONS"]
  }
```
- **allowedOriginPattern** 允许的origin，默认为 *
- **maxAgeSeconds** 预检请求的有效期
- **allowedHeaders** 服务器支持的头信息
- **allowedMethods** 服务器支持的HTTP方法

## API定义
API采用JSON格式来定义上层接口与下游服务直接的转发规则。
Api定义支持两种风格的路径

- 正则表达式，用于精确定义某个API
- Ant风格，用于定义下游服务的一组API

网关在查找API定义的时候优先查找正则表达式风格的API，如果匹配到合适的API，就不在继续查找Ant风格的API

正则表达式的一个最简单的例子
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
- **name** API的名称，在同一个网关里，这个名称必须唯一。相同的名称会被覆盖
- **method** API的HTTP方法，仅支持GET、POST、PUT、DELETE
- **path** API的地址，支持正则表达式匹配
- **endpoints** 下游服务的转发规则定义，JSON数字，一个API可以向多个下游服务转发。**考虑到分布式事务问题，建议只有GET请求才可以向多个下游服务转发**。为了满足不同的转发规则，我们定义了几个不同类型的Endpoint，稍后会详细介绍。
  **所有的下游服务的响应内容均要求是JSON格式**
### dummy
Dummy类型的Endpoint是最简单的endpoint，它不向下游服务转发请求，而是直接使用result作为返回。使用dummy，我们可以实现简单的ping-pong的健康检查功能。
dummy类型的endpoint只有三个属性
- **type** dummy
- **name** endpoint的名称，所有的endpoint都必须有名称，后面有一些插件需要依赖于这个名称实现。这个名称可以随意定义，只要在同一个API定义中唯一就可以。
- **result** dummy需要返回的JSON对象。目前不支持JSON数组
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
**policy** 
因为vert.x有三种类型的事件，所以我们也定义了三种不同的endpoint。通过policy区分pub-sub、point-point、req-resp。转发的事件内容都是HTTP请求的请求体，但是我们可以通过请求转换插件实现更多功能

对于eventbus的使用有一个需要注意的地方，eventbus并没有queryString的概念，所以如果API调用没有请求体，向下游的eventbus的转发的事件就是一个空的JSON对象，此时需要我们使用转换插件将queryString或者header加入到body中。

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
前面API定义章节已经描述了如何使用最核心的的转发功能，但是在实际业务中API网关还需要承载更多的功能，如鉴权、参数校验、限流等等，我们通过Plugin和Filter两个组件组合使用来实现各种不同的需求。Plugin和Filter组合起来才能发挥API网关的最大威力。
Filter分为两种PRE和POST
- PRE 在向下游服务转发请求前执行
- POST 在收到下游服务响应后执行

每个Filter内部都有一个order，API网关在收到请求之后会按顺序执行Filter。

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
