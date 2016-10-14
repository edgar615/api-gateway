package com.edgar.direwolves.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class RequestTransfomerFilterTest {

  Vertx vertx;

  String appKey = "abc";

  String appSecret = "123456";

  String scope = "all";

  int appCode = 0;

  String signMethod = "HMACMD5";

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
  }

  @Test
  public void testEmptySecret(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("sign", Randoms.randomAlphabetAndNum(16).toUpperCase());

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

    RequestTransfomerFilter filter = new RequestTransfomerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.INVALID_REQ, ex.getErrorCode());
      }
    });
  }


}
