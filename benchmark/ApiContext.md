每个Filter在处理完之后都会重新将ApiContext拷贝一个副本，用于跟踪Filter对ApiContext的修改。

经过测试发现，随着Plugin的增加，copy方法的性能会降低.

    Benchmark                                  Mode  Cnt      Score     Error   Units
     ApiContextNoPluginBenchmarks.testApi      thrpt   20  39835.814 ± 838.464  ops/ms
     ApiContextNoPluginBenchmarks.testAverage   avgt   20     26.188 ±   0.696   ns/op
     ApiContextOnPluginBenchmarks.testApi      thrpt   20  11059.729 ± 174.862  ops/ms
     ApiContextOnPluginBenchmarks.testAverage   avgt   20     95.484 ±   1.029   ns/op
     ApiContextTwoPluginBenchmarks.testApi      thrpt   20  6866.680 ± 62.661  ops/ms
     ApiContextTwoPluginBenchmarks.testAverage   avgt   20   145.328 ±  1.721   ns/op
     ApiContextThreePluginBenchmarks.testApi      thrpt   20  4869.882 ± 66.171  ops/ms
     ApiContextThreePluginBenchmarks.testAverage   avgt   20   209.661 ±  7.061   ns/op
     ApiContextFourPluginBenchmarks.testApi      thrpt   20  3788.434 ± 86.691  ops/ms
     ApiContextFourPluginBenchmarks.testAverage   avgt   20   252.142 ±  6.163   ns/op

**后来发现，上面的测试由于样本太少，预热不完全，并未发挥JIT的优势，重新修改测试方法之后的结果**

    Benchmark                                  Mode  Cnt        Score       Error   Units
    ApiContextNoPluginBenchmarks.testApi      thrpt   20  3562772.330 ± 20431.584  ops/ms
    ApiContextNoPluginBenchmarks.testAverage   avgt   20        0.276 ±     0.001   ns/op
    ApiContextOnPluginBenchmarks.testApi      thrpt   20  1349393.361 ± 31070.217  ops/ms
    ApiContextOnPluginBenchmarks.testAverage   avgt   20        0.777 ±     0.003   ns/op
    ApiContextTwoPluginBenchmarks.testApi      thrpt   20  920637.237 ± 6071.985  ops/ms
    ApiContextTwoPluginBenchmarks.testAverage   avgt   20       1.130 ±    0.008   ns/op
    ApiContextThreePluginBenchmarks.testApi      thrpt   20  708174.746 ± 3604.424  ops/ms
    ApiContextThreePluginBenchmarks.testAverage   avgt   20       1.548 ±    0.039   ns/op
    ApiContextFourPluginBenchmarks.testApi      thrpt   20  605800.302 ± 2052.366  ops/ms
    ApiContextFourPluginBenchmarks.testAverage   avgt   20       1.655 ±    0.008   ns/op

可以看出 虽然随着插件的增加，性能会有所下降，但已经不再像之前的测试那样急剧变化了，但是整体性能依然会随着插件的数量增加而降低。
考虑到ApiDefinition实际上是一个不可变对象（创建后不会修改），这里不再使用ApiDefinition.copy()方法，而是直接将ApiDefinition赋值给新的ApiContext

再次运行上面的测试

    Benchmark                                  Mode  Cnt         Score       Error   Units
    ApiContextNoPluginBenchmarks.testApi      thrpt   20  14676362.195 ± 56665.674  ops/ms
    ApiContextNoPluginBenchmarks.testAverage   avgt   20         0.064 ±     0.001   ns/op
    ApiContextOnPluginBenchmarks.testApi      thrpt   20  13643758.967 ± 88416.812  ops/ms
    ApiContextOnPluginBenchmarks.testAverage   avgt   20         0.083 ±     0.002   ns/op
    ApiContextTwoPluginBenchmarks.testApi      thrpt   20  11458923.663 ± 73077.244  ops/ms
    ApiContextTwoPluginBenchmarks.testAverage   avgt   20         0.077 ±     0.001   ns/op
    ApiContextThreePluginBenchmarks.testApi      thrpt   20  12039175.438 ± 82808.789  ops/ms
    ApiContextThreePluginBenchmarks.testAverage   avgt   20         0.088 ±     0.001   ns/op
    ApiContextFourPluginBenchmarks.testApi      thrpt   20  12642529.445 ± 104904.030  ops/ms
    ApiContextFourPluginBenchmarks.testAverage   avgt   20         0.088 ±      0.001   ns/op

可以看到性能已经不再像前面一样会有比较大的变化