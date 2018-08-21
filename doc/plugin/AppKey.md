## AppKey
一般开放平台的API都需要根据`App Key`和`App Secret`来验证调用方的合法性。
- **App Key** 应用唯一的识别标志，服务提供方通过App Key鉴别应用的身份
- **App Secret** 给应用分配的密钥，调用方需要妥善保存这个密钥，从而保证应用来源的的可靠性，防止被伪造。
- **App Id** 开发者的ID

AppKey和AppSecret是成对出现，同一个AppId可以对应多个AppKey+AppSecret，这样服务端就可以为调用方分配不一样的权限, 比如AppKey1+AppSecect1只有只读权限 但是AppKey2+AppSecret2有读写权限。通过将权限的配置与AppKey做关联，就可以把对应的权限开放给不同的开发者。

> 在网上看到过一个通俗易懂的说法：
>
> App Id：我的身份证
>
> App Key: 银行卡号
>
> App Secret：银行卡密码


### Plugin: AppKeyPlugin
表明API需要验证调用方合法性。
配置
```
"appKey": true
```
- true 表示开启这个插件
- false 表示关闭这个插件

### Filter: AppKeyFilter
通过appKey需要验证调用方合法性。如果签名校验未通过，会返回`1022`非法请求的错误。
校验通过后，会在上下文中存入四个变量，其他Filter可以根据这四个变量实现额外的功能：

- client_appKey 必填
- client_appId 可选
- client_appName 可选
- client_permissions 可选


这个Filter求调用方在queryString中加上下列参数：

- **type** PRE
- **order** 8000

**前置条件**：AppKeyPlugin开启

这个Filter求调用方在queryString中加上下列参数：

- **appKey** string	服务端为每个第三方应用分配的appKey
- **nonce** string 调用方生成的随机数，每次请求都应该不同，主要保证签名不可预测
- **signMethod** string 签名的摘要算法，可选值MD5、HMACSHA256、HMACSHA512、 HMACMD5
- **sign** string 根据签名算法生成的签名，规则后面描述

上面的参数缺一不可，缺少任何参数都会返回`1009`参数非法的错误。
配置
```
  "appkey": {
    "cacheEnable" : true,
    "expireAfterWrite" : 1800,
    "api": "/appkey/import",
    "data": [ {
      "appKey": "RmOI7jCvDtfZ1RcAkea1",
      "appSecret": "dbb0f95c8ebf4317942d9f5057d0b38e",
      "appName": "test",
      "appId": 0,
      "permissions": "all"
    }]
  }
```
- **cacheEnable** 是否开启缓存，默认false，如果配置了这个参数，会从缓存中查找appKey，依赖于缓存的实现
- **expireAfterWrite** 缓存的过期时间，默认1800秒
- **api** API地址，如果配置了这个参数，当缓存中没有找到对应的appKey时，会向通过这个地址发送GET请求，向下游服务请求AppKey，最终发送的请求为http://127.0.0.1:${port}/{api}?appKey=${appKey}
- **data** JSON数组，存放一些固定的AppKey。

一个AppKey有下列四个属性
- **appKey** 必选，字符串
- **appSecret** 必选 字符串
- **appId** 可选 字符串或整数
- **appName** 可选 应用名称
- **permissions** 可选  字符串 权限范围，多个权限范围用逗号","分隔，如`device:read,device:write`；如果每一个appKey的权限字符串不多，可以通过这个字符串来判断appKey的权限

**path的地址需要定义在API路由中，并且限制只能127.0.0.1的IP访问**

appKey的查找按照data -> cache -> api的顺序进行查找。
如果同时开启了cache和api，那么在cache没有找到对应的appKey但是api找到了对应的appKey，会将appKey存入缓存，从而减少对下游服务的请求。
同时为了避免缓存穿透问题，如果api也没有找到对应的appKey，会在缓存中存入一个不存在的key，来减小不存在的key对下游服务的请求

客户端生成生成签名的通用步骤如下：
1. 设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串stringA，如果请求带请求体，将请求体中的JSON对象转换为字符串之后按照body=JSON的格式加入到URL键值中，拼接成字符串stringA。
2. 对stringA按照signMethod中指定的算法进行加密得到最终的signValue。
3. 如果是MD5加密，**需要在stringA的首尾加上appSecret**。
4. 将sign=signValue追加到URL参数的后面，向服务端发送请求。

**下面通过两个例子介绍下sign的生成方式**
**示例1：GET请求 查询安防记录的接口**
```
GET /alarms?type=21&alarmTimeStart=1469280456&alarmTimeEnd=1471958856&start=0&limit=20
```
1. 增加通用参数
```
/alarms?type=21&alarmTimeStart=1469280456&alarmTimeEnd=1471958856&start=0&limit=20&appKey=XXXXX&nonce=123456&signMethod=HMACMD5
```
2. 将所有的参数排序得到新的查询字符串
```
alarmTimeEnd=1471958856&alarmTimeStart=1469280456&appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&type=21
```
3. 将上一步得到的查询字符串使用HMACMD5加密，得到签名7B686C90ACE0193430774F4BE096F128，并追加到查询参数之后
```
alarmTimeEnd=1471958856&alarmTimeStart=1469280456& appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&type=21 &sign= 7B686C90ACE0193430774F4BE096F128
```
4. 将上一步得到的查询字符串加入到接口中调用
```
/alarms? alarmTimeEnd=1471958856&alarmTimeStart=1469280456& appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&type=21&sign= 7B686C90ACE0193430774F4BE096F128
```

**示例2：POST请求 用户登录**
```
POST /login
{"username":"foo","password":"bar"}
```
1. 增加通用参数
```
/login?appKey=XXXXX&nonce=123456&signMethod=HMACMD5
```
2. 将请求体转换为JSON字符串后追加到参数列表中
```
appKey=XXXXX&nonce=123456&signMethod=HMACMD5&body={"username":"foo","password":"bar"}
```
3. 将所有的参数排序得到新的查询字符串
```
appKey=XXXXX&body={"username":"foo","password":"bar"}&nonce=123456&signMethod=HMACMD5
```
4. 将上一步得到的查询字符串使用HMACMD5加密，得到签名A61C44F04361DE0530F4EF2E363C4A45，并追加到查询参数之后（不包括body）
```
appKey=XXXXX&nonce=123456&signMethod=HMACMD5&sign= A61C44F04361DE0530F4EF2E363C4A45
```
5. 将上一步得到的查询字符串加入到接口中调用
```
/login?appKey=XXXXX&nonce=123456&signMethod=HMACMD5&sign= A61C44F04361DE0530F4EF2E363C4A45
```
### Plugin: AppKeyRestriction
表明API对调用方的appKey做了限制。
配置
```
"appKey.restriction" : {
	 "whitelist" : ["mv44GDQTqOAswwysqYsb", "bZfCBHyDnzf4lnYtGBEC],
	 "blacklist" : ["BzOCHhUkIdPUXYjQiWth"]
}
```
- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合白名单规则，都不允许继续请求
> *代表所有

**appKey不能同时存在于whitelist和blacklist，因为在设置某个appKey黑名单/白名单时会清除对应的白名单/黑名单**
### Filter: AppKeyRestrictionFilter
校验调用方是否能够访问对应的API。禁止调用方访问会返回1004的错误码：

- **type** PRE
- **order** 8100

**前置条件**：有AppKeyRestriction插件或者配置了`appKey.restriction`的全局参数
配置
```
"appKey.restriction" : {
	 "whitelist" : [],
	 "blacklist" : []
}
```
配置示例与AppKeyRestriction的类似。一旦配置了全局的参数，会对所有的API都有效，如果某个API需要存在例外，就可以通过AppKeyRestriction插件来添加例外