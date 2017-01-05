package com.edgar.direwolves.core.dispatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/9/12.
 *
 * @author Edgar  Date 2016/9/12
 */
@RunWith(VertxUnitRunner.class)
public class ApiContextTest {

  protected Vertx vertx;

  @Test
  public void testCopy(TestContext testContext) {
    Multimap<String, String > headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");
    Multimap<String, String > params = ArrayListMultimap.create();
    params.put("p1", "p1.1");
    params.put("p1", "p1.2");
    params.put("p2", "p2");
    ApiContext apiContext = ApiContext
            .create(HttpMethod.GET, "/devices", headers,
                    params, null);

    ApiContext copyContext = apiContext.copy();
    Assert.assertEquals(HttpMethod.GET, copyContext.method());
    Assert.assertEquals("/devices", copyContext.path());

  }

}
