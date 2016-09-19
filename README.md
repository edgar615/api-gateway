# direwolves
API网关,准备造的一个轮子

先使用Vert.x实现，在迁移到Openresty上


配置项
http.port int api的http端口
filter array 启用filter,会按照在数组中定义对顺序执行,可选值 jwt, app_key
jwt.keystore.path string jwt的证书文件路径 默认值keystore.jceks
jwt.keystore.type string 证书类型，可选值 jceks, jks,默认值jceks
jwt.keystore.password string 证书密钥，默认值secret
