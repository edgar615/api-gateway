还不确定怎么用JMH对Vert.x的异步方法做基准测试。

于是使用了闭锁等待测试方法的返回（可能不太准确）

# ApiDiscovery
APIDiscovery参考了ServiceDiscovery的实现，在内部使用一个Map——单机使用LocalMap，集群使用SyncMap，然后使用executeBlocking
方法来执行操作，发现每次查找API
都需要两到三毫秒。

所以针对使用LocalMap的场景做了测试
## 没有定义API
当没有定义任何路由时，基准测试的返回如下

    Benchmark                                                               Mode     Cnt     Score    Error   Units
    ApiDiscoveryEmptyApiBenchmarks.testThroughput                          thrpt      20  1147.767 ± 16.047  ops/ms
    ApiDiscoveryEmptyApiBenchmarks.testAverage                              avgt      20     0.001 ±  0.001   ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime                         sample  222703     0.001 ±  0.001   ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.00    sample             0.001            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.50    sample             0.001            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.90    sample             0.001            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.95    sample             0.001            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.99    sample             0.001            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.999   sample             0.003            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p0.9999  sample             0.013            ms/op
    ApiDiscoveryEmptyApiBenchmarks.testSampleTime:testSampleTime·p1.00    sample             0.081            ms/op

## 当有30多个API的时候

    Benchmark                                                        Mode    Cnt    Score    Error   Units
    ApiDiscoveryBenchmarks2.testApi                                 thrpt     20  135.207 ±  1.081  ops/ms
    ApiDiscoveryBenchmarks2.testAverage                              avgt     20    0.008 ±  0.001   ms/op
    ApiDiscoveryBenchmarks2.testSampleTime                         sample  27033    0.007 ±  0.001   ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.00    sample           0.006            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.50    sample           0.007            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.90    sample           0.008            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.95    sample           0.008            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.99    sample           0.010            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.999   sample           0.019            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p0.9999  sample           0.044            ms/op
    ApiDiscoveryBenchmarks2.testSampleTime:testSampleTime·p1.00    sample           0.056            ms/op

## 当有100多个API的时候

    Benchmark                                                        Mode    Cnt   Score    Error   Units
    ApiDiscoveryBenchmarks1.testApi                                 thrpt     20  56.269 ±  2.323  ops/ms
    ApiDiscoveryBenchmarks1.testAverage                              avgt     20   0.017 ±  0.001   ms/op
    ApiDiscoveryBenchmarks1.testSampleTime                         sample  10817   0.018 ±  0.001   ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.00    sample          0.016            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.50    sample          0.018            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.90    sample          0.020            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.95    sample          0.021            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.99    sample          0.029            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.999   sample          0.045            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p0.9999  sample          0.149            ms/op
    ApiDiscoveryBenchmarks1.testSampleTime:testSampleTime·p1.00    sample          0.155            ms/op

可以看到，随着API数量的增加ApiDiscovery模块的性能会跟着下降，这也与在线上的情况基本类似，一次API查找需要3毫秒左右。
因此ApiDiscovery模块应该还存在一些可以优化的地方。

从ApiDiscovery的实现来推测，性能损耗可能存在两个方面

1. 底层使用的Vert.x的LocalMap存储
2. 需要遍历LocalMap中所有的API，进行正则匹配

针对这两个方面分别测试：

1. 直接通过API的名称来过滤，不执行正则匹配，用来检查是否因为大量的正则造成的损耗
2. 使用一个HashMap来执行正则匹配，用来检查是否是LocalMap造成的损耗

查看直接使用名称过滤的测试结果，发现与使用正则匹配查询基本差不多，所以暂时不考虑这部分的优化：

    Benchmark                                                        Mode    Cnt   Score    Error   Units
    ApiDiscoveryBenchmarks3.testApi                                 thrpt     20  59.304 ±  0.280  ops/ms
    ApiDiscoveryBenchmarks3.testAverage                              avgt     20   0.017 ±  0.001   ms/op
    ApiDiscoveryBenchmarks3.testSampleTime                         sample  11369   0.018 ±  0.001   ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.00    sample          0.016            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.50    sample          0.017            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.90    sample          0.019            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.95    sample          0.020            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.99    sample          0.027            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.999   sample          0.041            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p0.9999  sample          0.356            ms/op
    ApiDiscoveryBenchmarks3.testSampleTime:testSampleTime·p1.00    sample          0.392            ms/op

查看使用HashMap的测试结果，发现性能有了显著提高，比空的LocalMap还要好。


    Benchmark                                                        Mode     Cnt      Score     Error   Units
    ApiDiscoveryBenchmarks4.testApi                                 thrpt      20  19767.853 ± 483.615  ops/ms
    ApiDiscoveryBenchmarks4.testAverage                              avgt      20     ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime                         sample  537603     ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.00    sample             ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.50    sample             ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.90    sample             ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.95    sample             ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.99    sample             ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.999   sample             ≈ 10⁻⁴             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p0.9999  sample              0.009             ms/op
    ApiDiscoveryBenchmarks4.testSampleTime:testSampleTime·p1.00    sample              0.077             ms/op

上面的测试结果说明LocalMap存在着少许性能损耗，而这里的性能损耗有可能是因为DefaultApiDefinitionBackend会使用executeBlocking操作LocalMap：
所以这里确定了两个优化方案：

1. 尝试取消DefaultApiDefinitionBackend的executeBlocking
2. 尝试使用一个HashMap缓存LocalMap中的API

基于下面两个原因，我决定采用第二种方式来优化：

1. vert.x的AsyncMap并没有提供getAll的方法，直接在SyncMap上取消executeBlocking，可能会降低整个eventloop的性能 （并未测试）
2. 未来会扩展Backend使用redis或者DB来存储，到时候依然会存在较多性能损耗


使用缓存的话需要考虑下面的问题：

如果每个API都先从本地缓存中寻找，缓存没有找到合适的API就到backend中查找，找到之后放入本地缓存。
由于API的比较一般都是通过路径进行正则匹配来查找，所以如果backedend中也没有找到对应的API，本地缓存无法写入一个NULL值——因为路径是可变的。所以对于不存在的API，会存在缓存穿透的问题

所以我们直接在本地缓存创建时就调用backend搜索到所有数据保存在本地缓存中，所有对API的操作之间在缓存中进行，不向backend发送请求——缓存数据的更新通过与backend之间的eventbus来通知。

增加缓存之后，性能有了显著提高：

    Benchmark                                                        Mode     Cnt      Score     Error   Units
    ApiLocalCacheBenchmarks.testApi                                 thrpt      20  18122.845 ± 140.583  ops/ms
    ApiLocalCacheBenchmarks.testAverage                              avgt      20     ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime                         sample  402978     ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.00    sample             ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.50    sample             ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.90    sample             ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.95    sample             ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.99    sample             ≈ 10⁻⁴             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.999   sample             ≈ 10⁻³             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p0.9999  sample              0.010             ms/op
    ApiLocalCacheBenchmarks.testSampleTime:testSampleTime·p1.00    sample              0.034             ms/op