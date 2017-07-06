package com.edgar.direwolves.handler;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.dispatch.BaseHandler;
import com.edgar.direwolves.dispatch.FailureHandler;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
@RunWith(VertxUnitRunner.class)
public class FailureHandlerTest {

  Vertx vertx;

  int port = Integer.parseInt(Randoms.randomNumber(4));

  @Before
  public void createServer(TestContext testContext) {
    vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(BaseHandler.create());
    router.get("/ex/sys").handler(rc -> {
      throw SystemException.create(DefaultErrorCode.MISSING_ARGS);
    });
    router.get("/ex/validate").handler(rc -> {
      Multimap<String, String> error = ArrayListMultimap.create();
      throw new ValidationException(error);
    });
    router.get("/ex/unkown").handler(rc -> {
      throw new RuntimeException("xxxx");
    });
    router.get("/ex/reply").handler(rc -> {
      throw new ReplyException(ReplyFailure.TIMEOUT, DefaultErrorCode.INVALID_REQ.getNumber(),
                               DefaultErrorCode.INVALID_REQ.getMessage());
    });

    router.get("/ex/reply2").handler(rc -> {
      throw new ReplyException(ReplyFailure.RECIPIENT_FAILURE);
    });
    router.route().failureHandler(FailureHandler.create());
    vertx.createHttpServer().requestHandler(router::accept)
            .listen(port, testContext.asyncAssertSuccess());
  }

  @After
  public void closeServer(TestContext testContext) {
    vertx.close();
  }

  @Test
  public void testSysException(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(port, "localhost", "/ex/sys",
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() == 400);
                     testContext.assertEquals(DefaultErrorCode.MISSING_ARGS.getNumber(), new
                             JsonObject(body.toString()).getInteger("code"));
                     async.complete();
                   });
                 }).end();
  }

  @Test
  public void testValidationException(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(port, "localhost", "/ex/validate",
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() == 400);
                     testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(), new
                             JsonObject(body.toString()).getInteger("code"));
                     async.complete();
                   });
                 }).end();
  }

  @Test
  public void testUnkownException(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(port, "localhost", "/ex/unkown",
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() == 500);
                     testContext.assertEquals(DefaultErrorCode.UNKOWN.getNumber(), new
                             JsonObject(body.toString()).getInteger("code"));
                     async.complete();
                   });
                 }).end();
  }

  @Test
  public void testReply(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(port, "localhost", "/ex/reply",
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() == 400);
                     testContext.assertEquals(DefaultErrorCode.INVALID_REQ.getNumber(), new
                             JsonObject(body.toString()).getInteger("code"));
                     async.complete();
                   });
                 }).end();
  }

  @Test
  public void testReplyUndefined(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(port, "localhost", "/ex/reply2",
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() == 400);
                     testContext.assertEquals(DefaultErrorCode.EVENTBUS_REJECTED.getNumber(), new
                             JsonObject(body.toString()).getInteger("code"));
                     async.complete();
                   });
                 }).end();
  }
}
