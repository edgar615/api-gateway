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
在启动时从文件中读取API定义
配置示例
```
{
  "api.discovery" : {
    "name" : "iotp-app",
    "publishedAddress" : "direwolves.api.published",
    "unpublishedAddress" : "direwolves.api.unpublished"
  },
  "path" : "H:/csst/java-core/trunk/06SRC/iotp-app/router/api/backend"
}
```
### path
API定义存放的路径
###  api.discovery
API发现组件的配置属性
- **publishedAddress**: 发布一个API后的广播地址
- **unpublishedAddress**: 删除一个API后的广播地址
- **name**: API发现模块的名称，api-discovery组件会使用这个名称在vert.x的共享数据中存储API信息。

**对于在集群模式下的的网关，不同业务的网关name、publishedAddress、unpublishedAddress三个属性不能相同，不然会导致API定义错乱**

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

## API定义
API采用JSON格式来定义上层接口与下游服务直接的转发规则。
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
- **name** API的名称，在同一个网关里，这个名称必须唯一。相同的名称会被覆盖
- **method** API的HTTP方法，仅支持GET、POST、PUT、DELETE
- **path** API的地址，支持正则表达式匹配，但是不支持ant格式的匹配
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

## 扩展
前面API定义章节已经描述了如何使用最核心的的转发功能，但是在实际业务中API网关还需要承载更多的功能，如鉴权、参数校验、限流等等，我们通过Plugin和Filter两个组件组合使用来实现各种不同的需求。Plugin和Filter组合起来才能发挥API网关的最大威力。
Filter分为两种PRE和POST
- PRE 在向下游服务转发请求前执行
- POST 在收到下游服务响应后执行

每个Filter内部都有一个order，API网关在收到请求之后会按顺序执行Filter。

### API路由匹配
当API网关收到调用方的请求后，首先需要通过api-discovery模块根据请求方法和请求地址匹配到对应的API定义才能继续处理请求。如果没有找到对应的API定义，会直接返回404。
我定义了两种API匹配方式：ApiFindFilter和GrayFilter
#### Filter: ApiFindFilter
默认使用的API查找。如果未找到对应的API，返回404（资源不存在），如果根据正则匹配到多个API，返回500（数据冲突）。

- **type** PRE
- **order** -2147482648 

**前置条件**：上下文中不存在API

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
2. 不同版本的API使用VersionPlugin定义版本
3. 定义一个带有HeaderGrayPlugin插件的API。（可以重新定义一个新的API，也可以在原有的API上定义，推荐前者）
#### Plugin: VersionPlugin
用于定义API版本的插件，建议直接用日期来表示版本（GrayFilter并没有实现复杂的版本比较）
配置
```
"version": "20171108"
```
- **version**，对应版本号，使用日期格式。

**注意：多版本共存是API的名称不能重复**
#### Plugin: HeaderGrayPlugin
用来声明基于请求头的灰度发布规则
配置
```
"gray.header": "floor" 
```
**gray.header**用来指明在未匹配到`x-api-version`声明的版本时时，采用哪种方式匹配API。
- **floor** 匹配最低的版本
- **ceil** 匹配最高的版本
#### Filter: HeaderGrayFilter
如果请求头中带有`x-api-version`则执行这个Filter。 如果未找到合适的API，返回404（资源不存在），如果匹配到多个API，返回500（数据冲突）。

- **type** PRE
- **order** -2147482748

**前置条件**：上下文中不存在API

在引入了plugin-gray包之后GrayFilter会在ApiFindFilter之前执行，如果通过GrayFilter找到了合适的API，那么ApiFindFilter不会再执行。

~~因为调用方的鉴权是在查找API之后进行的，所以无法实现基于用户的灰度规则，但是将来我们可以扩展基于用户IP、按比例放量的灰度规则~~
#### Filter: PathParamFilter
将API定义中的正则表达式与请求路径做匹配，然后将正则表达式所对应的值转换为对应的参数.
参数名为param0  0表示匹配的第0个字符串，从0开始计算；参数值为正则表达式在请求路径中的值.
所有的参数名将保存在上下文变量中，可以通过$var.param0变量来获得

- **type** PRE
- **order** -2147481648

**前置条件**：所有请求都会执行

示例：
```
"path": "/regex/([\\d+]+)/test/([\\w+]+)",
```
用户请求 `/regex/95624/test/hlu6duKrlM`经过`PathParamFilter`解析之后会在上下文中保存两个变量
```
$var.param0:"95624"
$var.param1:"hlu6duKrlM"}
```

### 认证 Authentication 
认证（Authentication ）是用来回答以下问题：
- 用户是谁
- 当前用户是否真的是他所代表的角色

API网关需要处理身份认证的问题，避免下游服务重复实现身份认证导致的耦合和重复工作。
我们采用JWT来做用户认证。JWT的生成可以由一个下游服务生成，也可以由API网关生成。下游服务的生成我们这里不做描述，下面我们来看下如何通过插件来生成JWT和校验JWT。
#### Plugin: JwtBuildPlugin
表明这个API在向调用方法返回前需要通过`JwtBuildFilter`来生成一个Token
配置
```
"jwt.build": true 
```
- true 表示开启这个插件
- false 表示关闭这个插件

#### Filter: JwtBuildFilter
根据响应体创建一个JWT，如何追加到响应体中。
这个请求要求响应体中包括一个`userId`属性，如果响应体中没有这个属性，可以通过替换插件替换

- **type** POST
- **order** 10000

**前置条件**：JwtBuildPlugin开启

它依赖两个配置`jwt.builder`和`keyStore`
jwt.builder配置
```
  "jwt.builder": {
    "expiresInSeconds" : 3600,
    "algorithm": "HS512",
    "audience" : ["test"],
    "subject": "app",
    "issuer" : "edgar615",
    "noTimestamp" : false,
    "header" : {},
    "emptyingField" : true,
    "claimKey": []
  }
```

- **expiresInSeconds** TOKEN过期秒数，可选，用来生成exp
- **algorithm** keyStore的算法，默认HS256
- **audience** 接收该JWT的一方，可选，JSON数组
- **subject** 该JWT所面向的用户，可选
- **issuer**  该JWT所面向的用户，可选
- **noTimestamp** 是否生成iat 默认true，不生成
- **header**  额外的头信息，可选
- **emptyingField** 生成TOKEN时清除其他属性，返回结果里仅仅包括token, 默认false
- **claimKey** 生成token时把除userId外的哪些属性存入claims， 可选

keyStore配置
```
    "keyStore" : {
      "path": "keystore.jceks",
      "type": "jceks",
      "password": "secret"
    }
```

- **path** keyStore的存放路径，默认keystore.jceks
- **type** keyStore的类型，默认 jceks
- **password** keyStore的类型的密码，默认INIHPMOZPO

keystore的生成方式
```
keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA256 -keysize 2048 -alias HS256 -keypass secret
keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA384 -keysize 2048 -alias HS384 -keypass secret
keytool -genseckey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg HMacSHA512 -keysize 2048 -alias HS512 -keypass secret
keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS256 -keypass secret -sigalg SHA256withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS384 -keypass secret -sigalg SHA384withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkey -keystore keystore.jceks -storetype jceks -storepass secret -keyalg RSA -keysize 2048 -alias RS512 -keypass secret -sigalg SHA512withRSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 256 -alias ES256 -keypass secret -sigalg SHA256withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 384 -alias ES384 -keypass secret -sigalg SHA384withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 360
keytool -genkeypair -keystore keystore.jceks -storetype jceks -storepass secret -keyalg EC -keysize 521 -alias ES512 -keypass secret -sigalg SHA512withECDSA -dname "CN=,OU=,O=,L=,ST=,C=" -validity 36
```

#### Plugin: AuthenticationPlugin
表明这个API需要对JWT进行校验
配置
```
"authentication": true
```
- true 表示开启这个插件
- false 表示关闭这个插件

#### Filter: AuthenticationFilter
从请求头中取出下面格式的TOKEN，然后进行JWT的校验。
```
Authorization:Bearer <token>
```

- **type** PRE
- **order** 10000

**前置条件**：AuthenticationPlugin开启

它依赖两个配置`jwt.auth`和`keyStore`
jwt.auth配置
```
  "jwt.auth": {
    "ignoreExpiration": false,
    "audiences": [],
    "issuer": "",
    "leeway": 0
  }
```

- **ignoreExpiration** 是否校验exp 可选，默认false
- **audiences** 校验aud，JSON数组，可选
- **issuer** 校验iss，可选
- **leeway** 允许调用方与服务端的偏差

keyStore配置与前面相同，不在描述。

AuthenticationFilter对JWT用户校验通过后会将claims保存到上下文的principal中

#### Filter: UserLoaderFilter
根据AuthenticationFilter的结果，从下游服务中拉取用户信息并更新到principal中。
为了提高性能，UserLoaderFilter内部使用了一个缓存来保存用户信息。

- **type** PRE
- **order** 11000

**前置条件**：上下文中存在principal

配置
```
  "user" : {
    "url" : "/users",
    "cache": {
      "type" : "local",
      "expireAfterWrite": 3600,
      "maximumSize": 5000
    }
  }
```

- **url** 对应的API地址，最终发送的请求为http://127.0.0.1:${port}/{url}?userId=${userId}
- **cache** 用户缓存的配置

cache
- **type** 缓存类型，local和redis两种，默认为local
- **expireAfterWrite** 缓存的过期时间，单位秒，默认1800
- **maximumSize** 缓存的最大数量，默认5000，redis类型的缓存这个配置无效

**loader的地址需要定义在API路由中，并且限制只能127.0.0.1的IP访问**


### 授权 Authorization
授权（Authorization）是用来回答以下问题：
- 用户A是否被授权访问资源R
- 用户A是否被授权执行P操作




### 断路器
## 缓存
后续更新
## CMD
后续更新
## Metric
后续更新
## 日志
后续更新
## 基准测试
后续更新

***************************************************************************
**华丽的分割线**
**下面的文字是很早零零散散写的，比较凌乱**
***************************************************************************
TODO:

对于GET请求，对于相同的请求可以做缓存、节流（throttleFirst，throttleLast）：在一个时间窗口内，如果有重复的请求正在处理，合并减少向后端服务发送请求

请求头校验

API版本：在响应头中增加API的版本，如果有过期时间说明过期时间

eventbus的全局替换

所有的全局插件配置均可以动态修改(配置管理，eventbus)

基于版本/用户的灰度发布（在nginx上处理可能更好）参考 http://www.ttlsa.com/linux/meizu-ad-http-api-sou/

## API Definition

重写eventbus类型的endpoint

监控

后台

打包
***还未找到更好的方法*
在整个开发编译过程中都不需要依赖hazelcast和logback组件，但是在在集群部署时依赖hazelcast，所以在打包的时候需要加入hazelcast，找到了三种方式：

1. 将hazelcast引入依赖（不喜欢）
2. 通过maven-jar-plugin增加Class-Path，然后通过`java -jar`启动

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



3. 通过-cp指定classpath，**java -jar会忽略-cp**，所以我们只能通过-cp来运行Main方法

java -cp "./*;ext/*;lib/*" io.vertx.core.Launcher run ServiceDiscoveryVerticle --cluster

**windows用;分隔,linux用:分隔**

配置项

- port int api的http端口
- filter array 启用filter,会按照在数组中定义对顺序执行,可选值 jwt, app_key
- keystore.path string 证书文件路径 默认值keystore.jceks
- keystore.type string 证书类型，可选值 jceks, jks,默认值jceks
- keystore.password string 证书密钥，默认值secret
- jwt.alg string jwt的加密算法,默认值HS512


    `HS256`:: HMAC using SHA-256 hash algorithm
    `HS384`:: HMAC using SHA-384 hash algorithm
    `HS512`:: HMAC using SHA-512 hash algorithm
    `RS256`:: RSASSA using SHA-256 hash algorithm
    `RS384`:: RSASSA using SHA-384 hash algorithm
    `RS512`:: RSASSA using SHA-512 hash algorithm
    `ES256`:: ECDSA using P-256 curve and SHA-256 hash algorithm
    `ES384`:: ECDSA using P-384 curve and SHA-384 hash algorithm
    `ES512`:: ECDSA using P-521 curve and SHA-512 hash algorithm


# Cache

# API查找



# IP限制
## Plugin: IpRestriction
对调用方的ip增加白名单和黑名单限制

配置示例：

    "ip.restriction" : {
         "whitelist" : ["192.168.0.1", "10.4.7.*"],
         "blacklist" : ["192.168.0.100"]
    }

- whitelist：白名单的数组，支持*的通配符，只要调用方的ip符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，支持*的通配符，只要调用方的ip符合黑名单规则，且不符合白名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: IpRestrictionFilter
调用方的ip从上下文读取`request.client_ip`变量

- type PRE
- order 7000

全局参数

    "ip.restriction" : {
      "blacklist": [],
      "whitelist": []
    }

示例

    "ip.restriction" : {
      "blacklist": ["10.4.7.15"],
      "whitelist": ["192.168.1.*"]]
    }

# AppKey校验
## Plugin: AppKeyPlugin
对调用方的appkey和sign做校验

配置示例：

     "appkey": true,

禁止访问对调用方会返回1022的错误码

## Filter: AppKeyFilter
调用方的ip从上下文读取`request.client_ip`变量

- type PRE
- order 8000

全局参数

    "appkey" : {
      data : APPKEY的JSON数组，默认为[]，
      url: http获取appkey的接口地址，这个地址对应了一个API路由,如果没有这个配置，则不会从后端查询appkey,
      cache: {
          "type" : "local", 缓存的类型：local，redis
          "expireAfterWrite": 1800, 缓存的过期时间，单位秒，默认值1800
          "maximumSize": 5000，缓存的最大数量，默认值5000
      }
    }

一个正确的appKey的JSON格式应该包括 appKey, appSecret, appCode, permissions四个属性。
如果通过HTTP获取appKey的下游服务返回的JSON格式不是这几个属性，可以通过ResponseTransformerPlugin进行转换

示例

      "appkey": {
        "data": [
          {
            "appKey": "RmOI7jCvDtfZ1RcAkea1",
            "appSecret": "dbb0f95c8ebf4317942d9f5057d0b38e",
            "appCode": 0,
            "permissions": "all"
          }
        ],
        "url": "/appkey/import"
      }

## 签名生成的通用步骤如下：
    第一步，设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串stringA，如果请求带请求体，将请求体中的JSON对象转换为字符串之后按照body=JSON的格式加入到URL键值中，拼接成字符串stringA。
    第二步，对stringA按照signMethod中指定的算法进行加密得到最终的signValue。
    如果是MD5加密，需要在stringA的首尾加上appSecret。
    第三步，将sign=signValue追加到URL参数的后面，向服务端发送请求。
### 示例1,GET请求 查询安防记录的接口
    GET /alarms?type=21&alarmTimeStart=1469280456&alarmTimeEnd=1471958856&start=0&limit=20
    第一步，增加通用参数
    /alarms?type=21&alarmTimeStart=1469280456&alarmTimeEnd=1471958856&start=0&limit=20&appKey=XXXXX&timestamp=1471958856&nonce=123456&v=1.0&signMethod=HMACMD5
    第二步，将所有的参数排序得到新的查询字符串
    alarmTimeEnd=1471958856&alarmTimeStart=1469280456&appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&timestamp=1471958856&type=21&v=1.0
    第三步，将上一步得到的查询字符串使用HMACMD5加密，得到签名7B686C90ACE0193430774F4BE096F128，并追加到查询参数之后
    alarmTimeEnd=1471958856&alarmTimeStart=1469280456& appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&timestamp=1471958856&type=21&v=1.0&sign= 7B686C90ACE0193430774F4BE096F128
    第四步，将上一步得到的查询字符串加入到接口中调用/alarms? alarmTimeEnd=1471958856&alarmTimeStart=1469280456& appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&timestamp=1471958856&type=21&v=1.0&sign= 7B686C90ACE0193430774F4BE096F128
###     示例2,POST请求 用户登录
    POST /login
    {"username":"foo","password":"bar"}
    第一步，增加通用参数
    /login?appKey=XXXXX&timestamp=1471958856&nonce=123456&v=1.0&signMethod=HMACMD5
    第二步，将请求体转换为JSON字符串后追加到参数列表中
    appKey=XXXXX&timestamp=1471958856&nonce=123456&v=1.0&signMethod=HMACMD5&body={"username":"foo","password":"bar"}
    第二步，将所有的参数排序得到新的查询字符串
    appKey=XXXXX&body={"username":"foo","password":"bar"}&nonce=123456&signMethod=HMACMD5&timestamp=1471958856&v=1.0
    第三步，将上一步得到的查询字符串使用HMACMD5加密，得到签名A61C44F04361DE0530F4EF2E363C4A45，并追加到查询参数之后（不包括body）
    appKey=XXXXX&nonce=123456&signMethod=HMACMD5&timestamp=1471958856&v=1.0&sign= A61C44F04361DE0530F4EF2E363C4A45
    第四步，将上一步得到的查询字符串加入到接口中调用
    /login?appKey=XXXXX&nonce=123456&signMethod=HMACMD5&timestamp=1471958856&v=1.0&sign= A61C44F04361DE0530F4EF2E363C4A45


# AppKey限制
## Plugin: AppKeyRestriction
对调用API的调用方，增加白名单和黑名单限制

配置示例：

    "appkey.restriction" : {
         "whitelist" : ["mv44GDQTqOAswwysqYsb", "bZfCBHyDnzf4lnYtGBEC],
         "blacklist" : ["BzOCHhUkIdPUXYjQiWth"]
    }

- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合白名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: AppKeyRestrictionFilter
调用方的appKey从上下文读取`app.appKey`变量。如果是没有定义appKey，永远成功，所以这个拦截器要放在AppKey校验的后面

- type PRE
- order 8100

全局参数

    "appkey.restriction" : {
         "whitelist" : [],
         "blacklist" : []
    }

示例

    "appkey.restriction" : {
         "whitelist" : ["mv44GDQTqOAswwysqYsb", "bZfCBHyDnzf4lnYtGBEC],
         "blacklist" : ["BzOCHhUkIdPUXYjQiWth"]
    }

# ACL限制
## Plugin: AclRestriction
对调用API的组（仅检查登录用户）增加白名单和黑名单限制

配置示例：

    "acl.restriction" : {
         "whitelist" : ["group1", "group2],
         "blacklist" : [guest]
    }

- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合白名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: AclRestrictionFilter
调用方的ip从上下文读取`user.group`变量。如果是未登陆用户，永远成功，所以这个拦截器要放在身份认证的后面

- type PRE
- order 12000

全局参数

    "acl.restriction" : {
      "blacklist": [],
      "whitelist": [],
      "groupKey": "group"
    }

示例

    "acl.restriction" : {
      "blacklist": ["guest],
      "whitelist": ["user],
      "groupKey": "role"
    }

# AppCode校验（项目的特殊需求）
## Plugin: AppCodeVertifyPlugin
校验appKey对应的appCode属性(上下文中的app.code)和用户对应的appCode属性(可以由app.codeKey指定)是否一致。

配置示例：

    "app.code.vertify": true

## Filter: AppCodeVertifyFilter

- type PRE
- order 10100

全局参数

    app.codeKey 编码的键值，默认值appCode

示例

    "app.codeKey" : "companyCode"

# 授权校验
## Plugin: AuthorisePlugin
校验对应的appkey或者用户是否有API的访问权限。

配置示例：

    "scope": "device:write"

device:write表示API的权限字符串

## Filter: AuthoriseFilter
如果调用方通过了AppKey的校验，会在上下文中保存`app.permissions`的变量，如果`app.permissions`中不包括接口的scope，拒绝访问.
如果调用方通过了身份认证的校验，会在用户中保存`permissions`的变量，如果`permissions`中不包括接口的scope，拒绝访问.

- type PRE
- order 11000

# Request降级
## Plugin: FallbackPlugin
支持远程调用的降级

配置示例：

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

- `add_device`  对应endpoint的名称
- statusCode 响应码
- result 响应体，可以是JsonObject或者JsonArray

## Filter: RequestFallbackFilter

- type PRE
- order 14000



# Request转换
## Plugin: RequestTransformerPlugin
将转发的请求参数按照一定的规则做转换

配置示例：

    "request.transformer": {
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

- name endpoint的名称，必填项，只有这个名称的endpoint才会执行参数转换
- header.remove 数组，需要删除的请求头
- query.remove 数组，需要删除的请求参数
- body.remove 数组，需要删除的请求体
- header.replace 数组，需要重命名的请求头，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- query.replace 数组，需要重命名的请求参数，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- body.replace 数组，需要重命名的请求体，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- header.add 数组，需要增加的请求头，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
- query.add 数组，需要增加的请求参数，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
- body.add 数组，需要增加的请求体，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值

执行的顺序：remove replace add

## Filter: RequestTransformerFilter

- type PRE
- order 15000

全局参数，对所有的请求都支持的参数转换

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


## Filter: HttpRequestReplaceFilter
用于将请求参数中的带变量的参数用变量的替换，一般与request.transformer结合使用

对于params和headers，如果新值是集合或者数组，将集合或数组的元素一个个放入params或headers，而不是将一个集合直接放入.(不考虑嵌套的集合),
例如：q1 : $header.h1对应的值是[h1.1, h1.2]，那么最终替换之后的新值是 q1 : [h1.1,h1.2]而不是 q1 : [[h1.1,h1.2]]

- type PRE
- order 2147482647.

示例

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

经过HttpRequestReplaceFilter之后，body中的元素包括

    "userId":1,
    "username":"edgar",
    "companyCode":0

header中的元素包括

    "x-auth-userId":"1",
    "x-auth-companyCode":"0",
    "x-policy-owner" : "individual"

# RPC调用
## Filter RpcFilter

- type PRE
- order int的最大值（用于在PRE的最后执行）

根据上下文中的request向下游服务发起远程调用。
HTTP调用支持断路器模式，eventbus暂不支持

全局参数，对所有的请求都支持的响应转换

     "circuit.breaker" : {
       "maxFailures" : 5,
       "maxRetries" : 0,
       "resetTimeout" : 60000,
       "timeout" : 3000,
       "metricsRollingWindow" : 10000,
       "notificationPeriod" : 2000,
       "notificationAddress" : "vertx.circuit-breaker",
       "registry" : "vertx.circuit.breaker.registry"
     }

- maxFailures  针对一个服务的请求失败多少次之后开启断路器，默认值5
- maxRetries 请求失败后的重试次数，默认值0
- resetTimeout 断路器打开之后，等待多长时间重置为半开状态，单位毫秒，默认值30000
- timeout 一个请求多长时间没有返回任务超时（失败）， 单位毫秒，默认值10000
- metricsRollingWindow 度量的时间窗口 单位毫秒，默认值10000
- notificationPeriod  通知周期，单位毫秒，默认值2000
- notificationAddress  通知地址，默认值vertx.circuit-breaker
- registry localmap中保存断路器的键值，默认值vertx.circuit.breaker.registry

# Response转换
## Plugin: ResponseTransformerPlugin
将响应的结果按照一定的规则做转换，目前还是比较简单的版本，还未完全实现.

配置示例：

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

- header.remove 数组，需要删除的响应头
- body.remove 数组，需要删除的响应体
- header.replace 数组，需要重命名的响应头，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- body.replace 数组，需要重命名的响应体，数组中每个元素的格式为h1:v1,其中h1表示需要被重命名的属性名，v1表示重命名后的属性名
- header.add 数组，需要增加的响应头，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值
- body.add 数组，需要增加的响应体，数组中每个元素的格式为h1:v1,其中h1表示键，v1表示值

## Filter ResponseTransformerFilter
- type POST
- order 1000

全局参数，对所有的请求都支持的响应转换

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

# 断路器
使用vert.x提供的断路器实现了简单的降级功能

配置

      "circuit.breaker": {
        "maxFailures": 5, //最大失败次数，一旦超过这个次数，会打开断路器
        "maxRetries": 0, //失败后重试的次数
        "resetTimeout": 60000, //断路器从打开恢复到半开的时间，单位毫秒
        "timeout": 3000, //请求的超时时间，单位毫秒，超过这个时间请求未结束会被认为是超时
        "metricsRollingWindow": 10000, //度量窗口的滑动间隔，单位毫秒
        "notificationPeriod": 2000, // 度量的通知间隔，单位毫秒
        "notificationAddress": "vertx.circuit-breaker", //度量的通知地址
        "cache.expires": 3600, //每个服务节点断路器的过期时间，单位秒，如果在这个时间内断路器没有被访问，会从缓存中删除断路器（下次使用时重新创建）
        "state.announce": "direwolves.circuitbreaker.announce"        //断路器状态变化后的会向这个地址发送广播，负载均衡服务可以订阅这个事件更新服务节点的状态
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

# 度量指标
启动时增加参数

    -Dvertx.metrics.options.enabled=true -Dvertx.metrics.options.registryName=my-registry

