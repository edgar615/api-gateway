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

删掉ApiDefinition的复制之后，性能有了显著提高，说明问题出现在这里

    if (source.apiDefinition() != null) {
      target.setApiDefinition(source.apiDefinition().copy());
    }

    Benchmark                                    Mode  Cnt       Score      Error   Units
    ApiContextFourPluginBenchmarks.testApi      thrpt   20  191261.896 ± 2889.066  ops/ms
    ApiContextFourPluginBenchmarks.testAverage   avgt   20       5.055 ±    0.059   ns/op

ApiDefinition的copy方法，会调用Decode和Encode方法.

先看Encode,经过一系列测试，发现问题可能出在在collect方法上

    factories.stream().filter(f -> this.name().equalsIgnoreCase(f.name()))
                .collect(Collectors.toList());

    Benchmark                                                              Mode     Cnt       Score    Error   Units
    ApiDefinitionEncodeBenchmarks.testApi                                 thrpt      20    5829.620 ± 18.215  ops/ms
    ApiDefinitionEncodeBenchmarks.testAverage                              avgt      20     166.230 ±  1.809   ns/op

修改代码后重新测试，性能有了提高差不多0.5倍（这个结果可能和环境有关，前一天测试时比原来的方法提高了3倍）：

    factories.stream().filter(f -> this.name().equalsIgnoreCase(f.name()))
                .map(f -> f.encode(this))
                .findFirst().orElseGet(() -> new JsonObject());

    Benchmark                                   Mode  Cnt     Score    Error   Units
    ApiDefinitionEncodeBenchmarks.testApi      thrpt   20  9655.723 ± 20.602  ops/ms
    ApiDefinitionEncodeBenchmarks.testAverage   avgt   20   102.481 ±  1.577   ns/op

Encode方法

Benchmark                                   Mode  Cnt      Score     Error   Units
ApiDefinitionDecodeBenchmarks.testApi      thrpt   20  16405.716 ± 268.513  ops/ms
ApiDefinitionDecodeBenchmarks.testAverage   avgt   20     62.505 ±   0.232   ns/op

经过多次测试，因为plugin的decode，encode需要从factory的列表中找到对应的factroy然后处理，这部分代码对性能的影响较大