package com.github.edgar615.gateway.circuitbreaker;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerState;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/8/25.
 *
 * @author Edgar  Date 2017/8/25
 */
@RunWith(VertxUnitRunner.class)
public class CircuitBreakerTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @Test
  public void testCircuitBreaker(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("maxFailures", 1)
            .put("timeout", 1000);
    CircuitBreakerRegistry registry =
            CircuitBreakerRegistry.create(vertx, new CircuitBreakerRegistryOptions(config));
    String name = UUID.randomUUID().toString();
    CircuitBreaker circuitBreaker = registry.get(name);

    Future<Void> future = circuitBreaker.execute(f -> {

    });

    future.setHandler(ar -> {
      if (future.succeeded()) {
        testContext.fail();
      } else {
        ar.cause().printStackTrace();
      }
    });

    Awaitility.await().until(() -> circuitBreaker.state() == CircuitBreakerState.OPEN);

  }

  @Test
  public void testCircuitBreakerReset(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("maxFailures", 1)
            .put("timeout", 1000)
            .put("resetTimeout", 1000);

    CircuitBreakerRegistry registry =
            CircuitBreakerRegistry.create(vertx, new CircuitBreakerRegistryOptions(config));
    String name = UUID.randomUUID().toString();
    CircuitBreaker circuitBreaker = registry.get(name);
    Future<Void> future = circuitBreaker.execute(f -> {

    });

    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        ar.cause().printStackTrace();
      }
    });

    Awaitility.await().until(() -> circuitBreaker.state() == CircuitBreakerState.OPEN);

    future = circuitBreaker.execute(f -> {

    });

    AtomicBoolean check2 = new AtomicBoolean();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        check2.set(true);
      }
    });

    Awaitility.await().until(() -> check2.get());

    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    future = circuitBreaker.execute(f -> {
      f.complete();
    });

    AtomicBoolean check3 = new AtomicBoolean();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        check3.set(true);
      } else {
        testContext.fail();
      }
    });

    Awaitility.await().until(() -> check3.get());
  }
}
