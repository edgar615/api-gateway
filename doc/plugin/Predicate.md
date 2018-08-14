##  PredicatePlugin
从spring cloud中借鉴的灵感，通过PredicatePlugin可以定义一些谓词，通过这些谓词可以根据用户特征限制只有部分用户能够访问这个接口。
通过它可以实现切流、A/B Testing，蓝绿部署等问题
PS：以前做的切流插件比较复杂

配置

```
{
  "predicate": {
    "before": "2018-08-05T17:05:02.717+08:00",
    "after": "2018-08-06T17:05:02.717+08:00",
    "between": [
      "2018-08-05T17:05:02.717+08:00",
      "2018-08-06T17:05:02.717+08:00"
    ],
    "header": {
      "contains": [
        "X-Api-Version",
        "Content-Type"
      ],
      "equals": {
        "X-Api-Version": "0.0.1"
      },
      "regex": {
        "X-Api-Version": "\\d+"
      }
    },
    "query": {
      "contains": [
        "version",
        "foo"
      ],
      "equals": {
        "X-Api-Version": "0.0.1"
      },
      "regex": {
        "X-Api-Version": "\\d+"
      }
    },
    "remoteAddr": {
      "appoint": [
        "192.168.1.5",
        "172.16.*"
      ],
      "hash": {
        "start": 70,
        "end": 80
      },
      "range": {
        "start": 3221225731,
        "end": 3232235781
      }
    }
  }
}
```

下面会分别介绍这些谓词
### 请求时间
时间的格式要求是ISO8601的时间格式

```
    "before": "2018-08-05T17:05:02.717+08:00",
    "after": "2018-08-06T17:05:02.717+08:00",
    "between": [
      "2018-08-05T17:05:02.717+08:00",
      "2018-08-06T17:05:02.717+08:00"
    ]
```

- before 在这个时间之前的请求为true
- after 在这个时间之后的请求为true
- between 字符串数组，第一个字符串为开始时间，第二个字符串为结束时间，在开始时间和结束时间之间的请求为true

### 请求头 

```
    "header": {
      "contains": [
        "X-Api-Version",
        "Content-Type"
      ],
      "equals": {
        "X-Api-Version": "0.0.1"
      },
      "regex": {
        "X-Api-Version": "\\d+"
      }
    }
```

- contains 包含某个请求头，字符串数组
- equals 检查请求头是否是指定的值
- regex 检查请求头是否符合指定的正则表达式

### 查询参数

```
   "query": {
      "contains": [
        "version",
        "foo"
      ],
      "equals": {
        "X-Api-Version": "0.0.1"
      },
      "regex": {
        "X-Api-Version": "\\d+"
      }
    }
```

- contains 包含某个参数，字符串数组
- equals 检查参数是否是指定的值
- regex 检查参数是否符合指定的正则表达式

### 调用方IP

```
    "remoteAddr": {
      "appoint": [
        "192.168.1.5",
        "172.16.*"
      ],
      "hash": {
        "start": 70,
        "end": 80
      },
      "range": {
        "start": 3221225731,
        "end": 3232235781
      }
    }
```

- appoint 包含某些IP，字符串数组，支持*的通配符
- hash 将IP进行HASH计算后，检查hash值是否匹配范围
    - start 最小值，默认值0
    - end 最大值，默认值100
- range 将IP转换为整数后，检查IP是否匹配范围
    - start 最小值，默认值0
    - end 最大值，默认值4294967295