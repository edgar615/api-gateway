##  ScopePlugin
很多时候，我们的API需要校验调用方是否有某个接口的访问权限，所以在定义API的时候，我们可以通过ScopePlugin来定义API的权限值，然后交由下游服务判断

配置

```
{
  "scope":"device:read"
}
```

- scope 权限范围，默认值default，default类型的权限值不做权限校验