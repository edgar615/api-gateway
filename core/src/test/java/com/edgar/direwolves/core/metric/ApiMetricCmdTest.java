package com.edgar.direwolves.core.metric;

import com.edgar.direwolves.core.cmd.ApiCmd;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class ApiMetricCmdTest {

  ApiCmd cmd;

  @Before
  public void setUp() {
    cmd = new ApiMetricCmdFactory().create(Vertx.vertx(), new JsonObject());
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testMetric(TestContext testContext) {
    Async async = testContext.async();
    cmd.handle(new JsonObject())
            .setHandler(ar -> {
              if (ar.succeeded()) {
                System.out.println(ar.result());
                async.complete();
              } else {
                testContext.fail();
              }
            });
  }

}
