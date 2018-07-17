package com.github.edgar615.gateway.dispatch;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.util.base.Randoms;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.After;
import org.junit.Before;
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

  protected int port = Integer.parseInt(Randoms.randomNumber(4));

  protected String host = "localhost";

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route()
            .handler(rc -> {
              ApiContext apiContext = ApiContextUtils.apiContext(rc);
              context.assertNotNull(apiContext);
              assertGetNoParam(context, rc, apiContext);
              assertGetTwoParam(context, rc, apiContext);
              assertGetToken(context, rc, apiContext);
              assertDeleteNoParam(context, rc, apiContext);
              assertDeleteTwoParam(context, rc, apiContext);
              assertDeleteToken(context, rc, apiContext);
              assertPostNoParam(context, rc, apiContext);
              assertPostTwoParam(context, rc, apiContext);
              assertPostToken(context, rc, apiContext);
              assertPutNoParam(context, rc, apiContext);
              assertPutTwoParam(context, rc, apiContext);
              assertPutToken(context, rc, apiContext);
              assertPostInvaidJson(context, rc, apiContext);
              rc.response().setChunked(true)
                      .end(new JsonObject()
                                   .put("foo", "bar").encode());
            }).failureHandler(FailureHandler.create());

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(port, context.asyncAssertSuccess());
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testGetNoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().get(port, host, "/get_no_param", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).end();
  }

  @Test
  public void testGetTwoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().get(port, host, "/get_two_param?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).end();
  }

  @Test
  public void testGetWithToken(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().get(port, host, "/get_token?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).putHeader("Authorization", "Bearer " + "token")
            .end();
  }

  @Test
  public void testDeleteNoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().delete(port, host, "/delete_no_param", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).end();
  }

  @Test
  public void testDeleteTwoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().delete(port, host, "/delete_two_param?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).end();
  }

  @Test
  public void testDeleteWithToken(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().delete(port, host, "/delete_token?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).putHeader("Authorization", "Bearer " + "token")
            .end();
  }

  @Test
  public void testPostNoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().post(port, host, "/post_no_param", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }

  @Test
  public void testPostTwoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().post(port, host, "/post_two_param?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }

  @Test
  public void testPostWithToken(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().post(port, host, "/post_token?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).putHeader("Authorization", "Bearer " + "token")
            .setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }

  @Test
  public void testPostInvaidJson(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().post(port, host, "/post_invalid_json?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).putHeader("Authorization", "Bearer " + "token")
            .setChunked(true).end("username=edgar");
  }

  @Test
  public void testPutNoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().put(port, host, "/put_no_param", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }

  @Test
  public void testPutTwoParam(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().put(port, host, "/put_two_param?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }

  @Test
  public void testPutWithToken(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().put(port, host, "/put_token?limit=10&start=1", response -> {
      response.bodyHandler(body -> {
        System.out.println(body);
        async.complete();
      });
    }).putHeader("Authorization", "Bearer " + "token")
            .setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }

  private void assertGetNoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/get_no_param")) {
      context.assertEquals("/get_no_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(0, apiContext.params().keySet().size());
      context.assertNull(apiContext.body());
      context.assertEquals(HttpMethod.GET, apiContext.method());
    }
  }

  private void assertGetTwoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/get_two_param")) {
      context.assertEquals("/get_two_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertNull(apiContext.body());
      context.assertEquals(HttpMethod.GET, apiContext.method());
    }
  }

  private void assertGetToken(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/get_token")) {
      context.assertEquals("/get_token", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertNull(apiContext.body());
      context.assertEquals(HttpMethod.GET, apiContext.method());
    }
  }

  private void assertDeleteNoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/delete_no_param")) {
      context.assertEquals("/delete_no_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(0, apiContext.params().keySet().size());
      context.assertNull(apiContext.body());
      context.assertEquals(HttpMethod.DELETE, apiContext.method());
    }
  }

  private void assertDeleteTwoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/delete_two_param")) {
      context.assertEquals("/delete_two_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertNull(apiContext.body());
      context.assertEquals(HttpMethod.DELETE, apiContext.method());
    }
  }

  private void assertDeleteToken(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/delete_token")) {
      context.assertEquals("/delete_token", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertNull(apiContext.body());
      context.assertEquals(HttpMethod.DELETE, apiContext.method());
    }
  }

  private void assertPostNoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/post_no_param")) {
      context.assertEquals("/post_no_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(0, apiContext.params().keySet().size());
      context.assertEquals("edgar", apiContext.body().getString("username"));
      context.assertEquals(HttpMethod.POST, apiContext.method());
    }
  }

  private void assertPostTwoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/post_two_param")) {
      context.assertEquals("/post_two_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertEquals("edgar", apiContext.body().getString("username"));
      context.assertEquals(HttpMethod.POST, apiContext.method());
    }
  }

  private void assertPostToken(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/post_token")) {
      context.assertEquals("/post_token", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertEquals("edgar", apiContext.body().getString("username"));
      context.assertEquals(HttpMethod.POST, apiContext.method());
    }
  }

  private void assertPostInvaidJson(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/post_invalid_json")) {
      context.assertEquals("/post_invalid_json", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertEquals("1024", apiContext.body().getString("code"));
      context.assertEquals(HttpMethod.POST, apiContext.method());
      context.assertFalse(apiContext.body().containsKey("username"));
    }
  }

  private void assertPutNoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/put_no_param")) {
      context.assertEquals("/put_no_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(0, apiContext.params().keySet().size());
      context.assertEquals("edgar", apiContext.body().getString("username"));
      context.assertEquals(HttpMethod.PUT, apiContext.method());
    }
  }

  private void assertPutTwoParam(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/put_two_param")) {
      context.assertEquals("/put_two_param", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertEquals("edgar", apiContext.body().getString("username"));
      context.assertEquals(HttpMethod.PUT, apiContext.method());
    }
  }

  private void assertPutToken(TestContext context, RoutingContext rc, ApiContext apiContext) {
    if (rc.normalisedPath().equalsIgnoreCase("/put_token")) {
      context.assertEquals("/put_token", apiContext.path());
      context.assertNotNull(apiContext.headers());
      context.assertEquals(2, apiContext.params().keySet().size());
      context.assertEquals("edgar", apiContext.body().getString("username"));
      context.assertEquals(HttpMethod.PUT, apiContext.method());
    }
  }
}
