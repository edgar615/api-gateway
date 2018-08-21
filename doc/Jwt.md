## 认证 Authentication
认证（Authentication ）是用来回答以下问题：
- 用户是谁
- 当前用户是否真的是他所代表的角色

API网关需要处理身份认证的问题，避免下游服务重复实现身份认证导致的耦合和重复工作。

TOKEN的生成有两种方式：
1. 网关生成，需要一组plugin和filter组合才能完成认证工作
2. 认证服务器生成，网关只是调用认证服务的接口校验TOKEN的合法性

对于网关生成TOKEN的方式，我们使用JWT来做用户的TOKEN。
下面我们来看下如何通过插件来生成JWT和校验JWT。
### Plugin: JwtBuildPlugin
表明这个API在向调用方法返回前需要通过`JwtBuildFilter`来生成一个Token
配置
```
"jwt.build": true
```
- true 表示开启这个插件
- false 表示关闭这个插件

### Filter: JwtBuildFilter
根据响应体创建一个JWT，如何追加到响应体中。
这个请求要求响应体中包括一个`userId`属性，如果响应体中没有这个属性，可以通过替换插件替换

- **type** POST
- **order** 20000

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

### Plugin: JwtPlugin
表明这个API需要对JWT进行校验
配置
```
"jwt": true
```
- true 表示开启这个插件
- false 表示关闭这个插件

### Filter: JwtFilter
从请求头中取出下面格式的TOKEN，然后进行JWT的校验。
```
Authorization:Bearer <token>
```

- **type** PRE
- **order** 10000

**前置条件**：AuthenticationPlugin开启

它依赖三个配置`jwt.auth`、`token`和`keyStore`
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

token配置
```
  "token": {
    "prefix": "Bearer ",
    "headerName" : "Authorization"
  }
```
- **headerName** token的请求头名称，默认为Authorization
- **prefix** 在token之前添加的额外字符串，默认为"Bearer "

keyStore配置与前面相同，不在描述。

AuthenticationFilter对JWT用户校验通过后会将claims保存到上下文的principal中

### Plugin: UserRestriction
表明API对调用方的userId做了限制。
配置
```
  "user.restriction" : {
    "blacklist" : ["3", "4"],
    "whitelist" : [2]
  }
```
- whitelist：白名单的数组，只要userId符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要userId符合黑名单规则，且不符合白名单规则，都不允许继续请求
> *代表所有

**userId不能同时存在于whitelist和blacklist，因为在设置某个userId黑名单/白名单时会清除对应的白名单/黑名单**
### Filter: UserRestrictionFilter
校验userId是否能够访问对应的API。禁止访问会返回1004的错误码：

- **type** PRE
- **order** 10500

**前置条件**：有UserRestriction插件或者配置了`user.restriction`的全局参数
配置
```
  "user.restriction" : {
    "blacklist" : ["3", "4"],
    "whitelist" : [2]
  }
```
配置示例与UserRestriction的类似。一旦配置了全局的参数，会对所有的API都有效，如果某个API需要存在例外，就可以通过UserRestriction插件来添加例外

### Plugin: AclRestriction
表明API对调用方所属组做了限制。
配置
```
  "acl.restriction" : {
    "blacklist" : ["anonymous", "ordinary", "testGroup1"],
    "whitelist" : ["admin"]
  }
```
- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合白名单规则，都不允许继续请求
> *代表所有

**group不能同时存在于whitelist和blacklist，因为在设置某个group黑名单/白名单时会清除对应的白名单/黑名单**
### Filter: AclRestrictionFilter
校验group是否能够访问对应的API。禁止访问会返回1004的错误码。
用户的group有两个来源：1.AuthenticationFilter通过后从TOKEN中解析的group，2.UserLoaderFilter从下游服务读取的group（如果读取到group会覆盖）。

> 我处理的业务来说group并不是有下游服务指定的，而是在创建token的时候通过替换插件直接指定的组。
>
> 如果用户没有group，默认为匿名用户`anonymous`

- **type** PRE
- **order** 12000

**前置条件**：有AclRestriction插件或者配置了`acl.restriction`的全局参数
配置
```
  "acl.restriction" : {
    "blacklist" : ["anonymous", "ordinary", "testGroup1"],
    "whitelist" : ["admin"]
  }
```
配置示例与AclRestriction的类似。一旦配置了全局的参数，会对所有的API都有效，如果某个API需要存在例外，就可以通过UserRestriction插件来添加例外

## 授权 Authorization
授权（Authorization）是用来回答以下问题：
- 用户A是否被授权访问资源R
- 用户A是否被授权执行P操作

### Plugin: ScopePlugin

校验对应的appkey或者用户是否有API的访问权限。

配置示例：

```
"scope": "device:write"

```

device:write表示API的权限字符串

为了减少API网关与下游服务直接的交互，调用方或用户拥有的权限在做身份认证的时候就从下游服务获取。
### Filter: AppKeyScopeFilter

如果调用方通过了AppKey的校验，会在上下文中保存`client_permissions`的变量(字符串)，如果`app.permissions`中不包括接口的scope，拒绝访问.

- type PRE
- order 11000


**前置条件**：ScopePlugin开启，且上下文中存在`client_permissions`变量

### Filter: UserScopeFilter
如果调用方通过了身份认证的校验，会在用户中保存`permissions`的变量(字符串)，如果`permissions`中不包括接口的scope，拒绝访问.

- type PRE
- order 11000
  **前置条件**：ScopePlugin开启，且上下文中存在用户变量

