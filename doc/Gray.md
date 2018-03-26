灰度发布 A/B Testing
对应服务如user，通过发布系统，在每次新版本上线时都将服务名修改为user-<newVersion>，然后通过灰度策略，将部分流量引导到user-<newVersion>

对应部分api，通过灰度策略，在请求头上设置不同的API版本，然后通过灰度策略，将部分流量引导到不同的版本上