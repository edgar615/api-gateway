## IP
### Plugin: IpRestriction
表明API对调用方的IP做了限制。
配置
```
"ip.restriction" : {
     "whitelist" : ["192.168.0.1", "10.4.7.*"],
     "blacklist" : ["192.168.0.100"]
}
```
- whitelist：白名单的数组，只要调用方所在组符合白名单规则，不管是否符合黑名单规则，都允许继续请求
- blacklist：黑名单的数组，只要调用方所在组符合黑名单规则，且不符合白名单规则，都不允许继续请求
> *代表所有

### Filter: IpRestrictionFilter
校验调用方的IP是否能够访问对应的API。禁止访问会返回1004的错误码。

- **type** PRE
- **order** 7000

**前置条件**：有IpRestriction插件或者配置了`ip.restriction`的全局参数
配置
```
"ip.restriction" : {
  "blacklist": ["10.4.7.15"],
  "whitelist": ["192.168.1.*"]]
}
```
配置示例与IpRestriction的类似。一旦配置了全局的参数，会对所有的API都有效，如果某个API需要存在例外，就可以通过IpRestriction插件来添加例外
