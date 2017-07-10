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

java -cp "./*;ext/*;lib/*" io.vertx.core.Launcher run ServiceDiscoveryVerticle --cluster

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

# API查找
## Filter: ApiFindFilter
根据请求地址，在API路由规则中寻找匹配的API。
如果未找到对应的API，范围资源不存在的异常。

- type PRE
- order -2147483648 （int的最小值）

# 路径参数（变量）
## Filter: PathParamFilter
将API定义中的正则表达式与请求路径做匹配，然后将正则表达式所对应的值转换为对应的参数.
参数名为param0  0表示第几个正则表达式，从0开始计算；参数值为正则表达式在请求路径中的值.
所有的参数名将保存在上下文变量中，可以通过$var.param0变量来获得

- type PRE
- order -2147483638

示例：

    API定义的路径为/devices/([\d+]+)，请求的路径为/devices/1，那么对应的参数名为param0，参数值为1

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
- order 5

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
- order 1100

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
- order 1100

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
- order 1010

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
- order 1100

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

# RPC调用
## Filter RpcFilter

- type PRE
- order int的最大值（用于在PRE的最后执行）

根据上下文中的request向下游服务发起远程调用。
HTTP调用支持断路器模式，eventbus暂不支持

全局参数，对所有的请求都支持的响应转换

- circuit.breaker.maxFailures  针对一个服务的请求失败多少次之后开启断路器，默认值5
- circuit.breaker.maxRetries 请求失败后的重试次数，默认值0
- circuit.breaker.resetTimeout 断路器打开之后，等待多长时间重置为半开状态，单位毫秒，默认值30000
- circuit.breaker.timeout 一个请求多长时间没有返回任务超时（失败）， 单位毫秒，默认值10000
- circuit.breaker.metricsRollingWindow 度量的时间窗口 单位毫秒，默认值10000
- circuit.breaker.notificationPeriod  通知周期，单位毫秒，默认值2000
- circuit.breaker.notificationAddress  通知地址，默认值vertx.circuit-breaker
- circuit.breaker.registry localmap中保存断路器的键值，默认值vertx.circuit.breaker.registry

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
