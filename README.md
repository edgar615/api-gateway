# direwolves
API网关,准备造的一个轮子

使用Vert.x实现

TODO:

对于GET请求，对于相同的请求可以做缓存、节流（throttleFirst，throttleLast）：在一个时间窗口内，如果有重复的请求正在处理，合并减少向后端服务发送请求

将缓存部分实现redis和clustermap（localmap）两种模式

request size limit(全局和单独)：限制过大流量的请求

request termination 中断请求，用来做后端接口的升级维护

请求头校验

断路器

API版本：在响应头中增加API的版本，如果有过期时间说明过期时间

所有的全局插件配置均可以动态修改(配置管理，eventbus)

多种类型日志
暂定的日志事件：

## API Definition
config.readed 读取配置
api.import 导入API
api.imported 导入API完成

api.added 添加API
api.deleted 删除
api.all 查找所有
api.finded 根据名称查找API

HttpRpcRequested:内部的HTTP转发
TokenCreated：创建token
TokenUpdated：更新token
TokenDeleted：注销token
AuthFailure：身份认证失败
RateLimitExceeded：超过限流
QuotaExeeded：配额超过
BreakerTripped：断路器开启
BreakerReset：断路器重置
SlowReqDetected：慢请求检查

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

java -cp "./*;ext/*;lib/*" io.vertx.core.Launcher run com.edgar.service.discovery.verticle.ServiceDiscoveryVerticle --cluster

**windows用;分隔,linux用:分隔**

配置项

- http.port int api的http端口
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

- jwt.audience string token的客户aud
- jwt.issuer string token的发行者iss
- jwt.subject string token的主题sub
- token.expires int 过期时间exp，单位秒，默认值1800
- timestamp_check.expires int 请求的过期时间,单位秒，默认值300

# IP限制
## Plugin: IpRestriction
对调用方的ip增加白名单和黑名单限制

配置示例：

    "ip.restriction" : {
         "whitelist" : ["192.168.0.1", "10.4.7.*"],
         "blacklist" : ["192.168.0.100"]
    }

- whitelist：白名单的数组，支持*的通配符，只要调用方的ip符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，支持*的通配符，只要调用方的ip符合黑名单规则，且不符合黑名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: IpRestrictionFilter
调用方的ip从上下文读取`request.client_ip`变量

- type PRE
- order 5

全局参数

    "ip.blacklist": [],
    "ip.whitelist": []

示例

    "ip.blacklist": ["10.4.7.15"],
    "ip.whitelist": ["192.168.1.*"]

# AppKey限制
## Plugin: AppKeyRestriction
对调用API的调用方，增加白名单和黑名单限制

配置示例：

    "appkey.restriction" : {
         "whitelist" : ["mv44GDQTqOAswwysqYsb", "bZfCBHyDnzf4lnYtGBEC],
         "blacklist" : ["BzOCHhUkIdPUXYjQiWth"]
    }

- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合黑名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: AppKeyRestrictionFilter
调用方的appKey从上下文读取`app.appKey`变量。如果是没有定义appKey，永远成功，所以这个拦截器要放在AppKey校验的后面

- type PRE
- order 1100

全局参数

    "appkey.blacklist": [],
    "appkey.whitelist": []

示例

    "appkey.blacklist": ["guest"],
    "appkey.whitelist": ["user"]


# ACL限制
## Plugin: AclRestriction
对调用API的组（仅检查登录用户）增加白名单和黑名单限制

配置示例：

    "acl.restriction" : {
         "whitelist" : ["group1", "group2],
         "blacklist" : [guest]
    }

- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合黑名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: AclRestrictionFilter
调用方的ip从上下文读取`user.group`变量。如果是未登陆用户，永远成功，所以这个拦截器要放在身份认证的后面

- type PRE
- order 1100

全局参数

    "acl.blacklist": [],
    "acl.whitelist": []
    "user.groupKey" 编码的键值，默认值group

示例

    "acl.blacklist": ["guest"],
    "acl.whitelist": ["user"],
    "user.groupKey" : "role"


# AppCode校验
## Plugin: AppCodeVertifyPlugin
校验appKey对应的appCode属性(上下文中的app.code)和用户对应的appCode属性(可以由app.codeKey指定)是否一致。

配置示例：

    "app.code.vertify": true

## Filter: AppCodeVertifyFilter

- type PRE
- order 1010

全局参数

    app.codeKey 编码的键值，默认值appCode

示例

    "app.codeKey" : "companyCode"

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
- order 2147483647，int的最大值.

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

# 日志

## ---> 表示API请求
格式

	---> [x-request-id] [HTTP] [HTTP方法 请求地址] [请求头，用;分隔，无请求头输出no header] [请求参数，用;分隔，无参数输出no param] [请求体，无请求体输出no body]

示例

	---> [d3af2bf8-b640-4c6a-97fb-d3181d83f941] [http] [GET /appkey/import] [content-type:application/json;Host:127.0.0.1:9003;] [no param] [no body]

## ---| 表示内部调用过程
格式

	---| [x-request-id] [OK | FAILED] [方法] [描述]

示例

	---| [3682506f-5587-4ff7-83b5-302e91f977c0] [OK] [ApiFindFilter] [PRE]
	---| [3682506f-5587-4ff7-83b5-302e91f977c0] [OK] [ResponseTransformerFilter] [POST]
	---| [be923e13-972d-4bf0-b5c0-62a4b876026d] [FAILED] [ApiFindFilter] [failed match api]

## ------> 表示远程调用
### HTTP格式

	------> [远程调用ID] [HTTP] [远程调用地址] [HTTP方法 请求地址] [请求头，用;分隔，无请求头输出no header] [请求参数，用;分隔，无参数输出no param] [请求体，无请求体输出no body]

示例

	------> [d3af2bf8-b640-4c6a-97fb-d3181d83f941.1] [HTTP] [localhost:52624] [GET /companies] [x-request-id:d3af2bf8-b640-4c6a-97fb-d3181d83f941.1;] [limit:100;start:0;state:1;] [no body]

### DUMMY格式

	------> [远程调用ID] [DUMMY] [JSON对象]

示例

	------> [0c7963ab-3126-40e3-8305-92fb6fa42269.1] [DUMMY] [{"userId":-188,"username":"backend","permissions":"all","role":"backend"}]

### Eventbus格式

	------> [远程调用ID] [EVENTBUS] [pub-sub | point-point | req-resp] [事件地址] [事件头，用;分隔，无请求头输出no header] [请求体，无请求体输出no body]

示例

	------> [1da1a8a5-94c8-4492-a623-fbe3164b5faf.1] [EVENTBUS] [req-resp] [example.direwolves.eb.api.list] [no header] [{}]

## <------ 表示远程调用返回
### HTTP格式

	<------ [远程调用ID] [HTTP] [OK|FAILED] [响应码] [耗时] [响应字节数]

示例

	<------ [d3af2bf8-b640-4c6a-97fb-d3181d83f941.1] [HTTP] [OK] [200] [18ms] [4807 bytes]

### DUMMY格式

	<------ [远程调用ID] [DUMMY] [OK|FAILED] [耗时] [响应字节数]

示例

	<------ [0c7963ab-3126-40e3-8305-92fb6fa42269.1] [DUMMY] [OK] [0ms] [73 bytes]

### Eventbus格式

	<------ [远程调用ID] [EVENTBUS] [OK|FAILED] [耗时] [响应字节数]

示例

	<------ [1da1a8a5-94c8-4492-a623-fbe3164b5faf.1] [EVENTBUS] [OK] [18ms] [3991 bytes]

## <--- 表示API响应
格式

	<--- [x-request-id] [http] [响应码] [响应头，用;分隔，无请求头输出no header] [耗时] [响应字节数]

示例

	<--- [d3af2bf8-b640-4c6a-97fb-d3181d83f941] [http] [200] [content-type:application/json;charset=utf-8;x-request-id:d3af2bf8-b640-4c6a-97fb-d3181d83f941;Transfer-Encoding:chunked;x-response-time:37ms;] [38ms] [4807 bytes]

## ======> 表示发送消息
格式

	======> [消息ID] [类型：MESSAGE | REQUEST | RESPONSE] [OK | FAILED] [消息主题或地址] [消息标识] [消息头，没有消息头的显示no header] [消息内容，没有消息内容的显示no body]

示例

	======> [4f82021b-84b8-44a1-9b0f-6d4b024b966d] [MESSAGE] [OK] [user-1ddd54a] [user.insert] [header{from=user-12, to=user-1ddd54a, group=user, action=MESSAGE, id=4f82021b-84b8-44a1-9b0f-6d4b024b966d, timestamp=1489650972, sequence=null}] [Message{content={foo=bar}, resource=user.insert, caption=insert, description=null}]

## <====== 表示收到的消息
格式

	<====== [消息ID] [类型MESSAGE | REQUEST | RESPONSE] [消息主题或地址] [消息标识]  [消息头，没有消息头的显示no header] [消息内容，没有消息内容的显示no body]

示例

	  <====== [8eea6dc7-17d5-4ce8-a36d-f8a9a3514b6a] [MESSAGE] [user-1ddd54a] [user.insert] [header{from=user-12, to=user-1ddd54a, group=user, action=MESSAGE, id=8eea6dc7-17d5-4ce8-a36d-f8a9a3514b6a, timestamp=1489650972, sequence=null}] [Message{content={foo=bar}, resource=user.insert, caption=insert, description=null}]
