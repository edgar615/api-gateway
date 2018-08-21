package com.github.edgar615.gateway.benchmark.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/17.
 * Benchmark                          Mode  Cnt        Score        Error   Units
 * ValidationBenchmarks.testApi      thrpt   20  2147067.321 ± 140865.945  ops/ms
 * ValidationBenchmarks.testAverage   avgt   20        0.488 ±      0.009   ns/op
 *
 * @author Edgar  Date 2017/7/17
 */
@State(Scope.Benchmark)
public class ValidationBenchmarks {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(1)
    @OperationsPerInvocation(10000)
    public void testApi() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("encryptKey", "0000000000000000");
        data.put("barcode", "LH10312ACCF23C4F3A5");

        Multimap<String, Rule> rules = ArrayListMultimap.create();

        rules.put("barcode", Rule.required());
        rules.put("barcode", Rule.regex("[0-9A-F]{16}"));
        rules.put("encryptKey", Rule.required());
        rules.put("encryptKey", Rule.regex("LH[0-7][0-9a-fA-F]{2}[0-5][0-4][0-9a-fA-F]{12}"));
        try {
            Validations.validate(data, rules);
        } catch (Exception e) {
        }

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(1)
    @OperationsPerInvocation(10000)
    public void testAverage() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("encryptKey", "0000000000000000");
        data.put("barcode", "LH10312ACCF23C4F3A5");

        Multimap<String, Rule> rules = ArrayListMultimap.create();

        rules.put("barcode", Rule.required());
        rules.put("barcode", Rule.regex("[0-9A-F]{16}"));
        rules.put("encryptKey", Rule.required());
        rules.put("encryptKey", Rule.regex("LH[0-7][0-9a-fA-F]{2}[0-5][0-4][0-9a-fA-F]{12}"));
        try {
            Validations.validate(data, rules);
        } catch (Exception e) {
        }
    }

}
