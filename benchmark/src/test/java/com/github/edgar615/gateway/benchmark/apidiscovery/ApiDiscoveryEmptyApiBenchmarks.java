package com.github.edgar615.gateway.benchmark.apidiscovery;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/12.
 *
 * @author Edgar  Date 2017/7/12
 */
@State(Scope.Benchmark)
public class ApiDiscoveryEmptyApiBenchmarks {

    @TearDown(Level.Trial)
    public void tearDown(EmptyApiBackend pool) {
        pool.close();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(1)
    @OperationsPerInvocation(10000)
    public void testThroughput(EmptyApiBackend backend) {
        final CountDownLatch latch = new CountDownLatch(1);
        backend.getDefinitions(new JsonObject().put("method", "GET").put("path", "/devices/1"),
                               ar -> {
                                   latch.countDown();
                               });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(1)
    @OperationsPerInvocation(10000)
    public void testAverage(EmptyApiBackend backend) {
        final CountDownLatch latch = new CountDownLatch(1);
        backend.getDefinitions(new JsonObject().put("method", "GET").put("path", "/devices/1"),
                               ar -> {
                                   latch.countDown();
                               });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @State(Scope.Benchmark)
    public static class EmptyApiBackend {
        private Vertx vertx;

        private ApiDiscovery apiDiscovery;

        public EmptyApiBackend() {
            vertx = Vertx.vertx();
            apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void getDefinitions(JsonObject jsonObject,
                                   Handler<AsyncResult<List<ApiDefinition>>>
                                           handler) {
            apiDiscovery.getDefinitions(jsonObject, handler);
        }

        public void close() {
            vertx.close();
        }
    }

//  @Benchmark
//  @BenchmarkMode(Mode.SampleTime)
//  @OutputTimeUnit(TimeUnit.NANOSECONDS)
//  @Fork(1)
//  @OperationsPerInvocation(100)
//  public void testSampleTime(ApiBackend backend) {
//    final CountDownLatch latch = new CountDownLatch(1);
//    backend.getDefinitions(new JsonObject().put("method", "GET").put("path", "/devices/1"), ar -> {
//      latch.countDown();
//    });
//    try {
//      latch.await();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
}
