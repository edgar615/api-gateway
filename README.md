# direwolves
API网关,准备造的一个轮子

先使用Vert.x实现，在迁移到Openresty上


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
- jwt.expires int 过期时间exp，单位秒，默认值1800
- timestamp_check.expires int 请求的过期时间,单位秒，默认值300