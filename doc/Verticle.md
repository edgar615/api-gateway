# Verticle
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
      "class": "ApiDefinitionVerticle",
      "instances": 1,
      "worker": false,
      "config": {
        
      }
    },
    "ApiDispatchVerticle": {
      "class": "ApiDispatchVerticle",
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
      "class": "ApiDispatchVerticle",
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
      "class": "RedisVerticle",
      "instances": 1,
      "worker": false,
      "config": {

      }
    },
    "ApiDispatchVerticle": {
      "class": "ApiDispatchVerticle",
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
    "publishedAddress" : "__com.github.edgar615.gateway.api.published",
    "unpublishedAddress" : "__com.github.edgar615.gateway.api.unpublished"
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

FileApiDiscoveryVerticle订阅`__com.github.edgar615.gateway.api.discovery.reload.file`事件，在接收到这个事件后会重新加载ApiDiscovery
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
      "address" : "__com.github.edgar615.gateway.api.discovery.reload.file"
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
  "path" : "H:/api"
}
```
- url GitHub存放API定义的地址
- branch 分支 默认master
- remote 远程地址 默认origin
- path 本地clone的目录

ApiGitVerticle订阅`__com.github.edgar615.gateway.api.discovery.git`事件，在接收到这个事件后会自动从GitHub上pull数据，然后通知FileApiDiscoveryVerticle刷新路由，所以我们也可以配置一个API路由来实现手动pull
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
      "address" : "__com.github.edgar615.gateway.api.discovery.git"
    }
  ]
}
```
通过这个功能，我们可以通过GitHub的钩子，在push之后手动触发刷新功能

###  api.discovery
API发现组件的配置属性
- **publishedAddress**: 发布一个API后的广播地址
- **unpublishedAddress**: 删除一个API后的广播地址

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