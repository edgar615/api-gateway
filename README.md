# direwolves
API网关,准备造的一个轮子

使用Vert.x实现

TODO:
把defintion的registry用vert.x的map实现（参考服务发现的实现）
definiton的Verticle判断当前是否加载了api，整个集群中只加载一次

definition的写入和读取分离

对于GET请求，对于相同的请求可以做缓存、节流（throttleFirst，throttleLast）：在一个时间窗口内，如果有重复的请求正在处理，合并减少向后端服务发送请求

将缓存部分实现redis和clustermap（localmap）两种模式

请求数控制和请求频率控制（令牌桶）两种实现

监控值

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

    "ip_restriction" : {
         "whitelist" : ["192.168.0.1", "10.4.7.*"],
         "blacklist" : ["192.168.0.100"]
    }

whitelist：白名单的数组，支持*的通配符，只要调用方的ip符合白名单规则，不管是否符合黑名单规则，都允许继续请求
blacklist：黑名单的数组，支持*的通配符，只要调用方的ip符合黑名单规则，且不符合黑名单规则，都不允许继续请求

禁止访问对调用方会返回1004的错误码

## Filter: IpRestrictionFilter
调用方的ip从上下文读取`request.client_ip`变量

- type PRE
- order 100

全局参数

    "ip.blacklist": [],
    "ip.whitelist": []

示例

    "ip.blacklist": ["10.4.7.15"],
    "ip.whitelist": ["192.168.1.*"]

# AppCode校验
## Plugin: AppCodeVertifyPlugin
校验appKey对应的appCode属性(上下文中的app.code)和用户对应的appCode属性(可以由app.codeKey指定)是否一致。

配置示例：

    "app_code_vertify": true

## Filter: AppCodeVertifyFilter

- type PRE
- order 1010

全局参数

    app.codeKey 编码的键值，默认值appCode

示例

    "app.codeKey" : "companyCode"

# http请求参数替换
## Filter: HttpRequestReplaceFilter
用于将请求参数中的带变量的参数用变量的替换，一般与request_transformer结合使用

对于params和headers，如果新值是集合或者数组，将集合或数组的元素一个个放入params或headers，而不是将一个集合直接放入.(不考虑嵌套的集合),
例如：q1 : $header.h1对应的值是[h1.1, h1.2]，那么最终替换之后的新值是 q1 : [h1.1,h1.2]而不是 q1 : [[h1.1,h1.2]]

- type PRE
- order 2147483647，int的最大值.

示例

    "request_transformer": [
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
