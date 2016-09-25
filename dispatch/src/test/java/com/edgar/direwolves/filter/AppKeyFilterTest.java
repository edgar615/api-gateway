package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
public class AppKeyFilterTest {

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
    public void testAppKeyParam(TestContext testContext) {

        JsonArray appKeys = new JsonArray();
        appKeys.add(new JsonObject()
                .put("key", appKey)
                .put("secret", appSecret)
                .put("scope", "all")
                .put("appCode", 0));
        JsonObject config = new JsonObject().put("app_key.secret", appKeys);

        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .build();

        AppKeyFilter filter = new AppKeyFilter();
        filter.config(vertx, config);

        Future<ApiContext> future = Future.future();
        try {
            filter.doFilter(apiContext, future);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testAppKeyParamErrorSign(TestContext testContext) {

        JsonArray appKeys = new JsonArray();
        appKeys.add(new JsonObject()
                .put("key", appKey)
                .put("secret", appSecret)
                .put("scope", "all")
                .put("appCode", 0));
        JsonObject config = new JsonObject().put("app_key.secret", appKeys);

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("appKey", appKey);
        params.put("nonce", Randoms.randomAlphabetAndNum(10));
        params.put("signMethod", signMethod);
        params.put("v", "1.0");
        params.put("sign", Randoms.randomAlphabetAndNum(16).toUpperCase());

        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setParams(params)
                .build();

        AppKeyFilter filter = new AppKeyFilter();
        filter.config(vertx, config);

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

        Async async = testContext.async();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                testContext.fail();
            } else {
                Throwable e = ar.cause();
                testContext.assertTrue(e instanceof SystemException);
                SystemException ex = (SystemException) e;
                testContext.assertEquals(DefaultErrorCode.INVALID_REQ.getNumber(), ex.getErrorCode().getNumber());

                async.complete();
            }
        });
    }

    @Test
    public void testAppKeyParamAndBody(TestContext testContext) {

        JsonArray appKeys = new JsonArray();
        appKeys.add(new JsonObject()
                .put("key", appKey)
                .put("secret", appSecret)
                .put("scope", "all")
                .put("appCode", 0));
        JsonObject config = new JsonObject().put("app_key.secret", appKeys);

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("appKey", appKey);
        params.put("nonce", Randoms.randomAlphabetAndNum(10));
        params.put("signMethod", signMethod);
        params.put("v", "1.0");
        params.put("deviceId", "1");

        JsonObject body = new JsonObject()
                .put("name", "$#$%$%$%")
                .put("code", 123434);

        params.put("body", body.encode());
        params.put("sign", signTopRequest(params, appSecret, signMethod));
        params.removeAll("body");

        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.POST)
                .setPath("/devices")
                .setParams(params)
                .setBody(body)
                .build();

        AppKeyFilter filter = new AppKeyFilter();
        filter.config(vertx, config);

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

        future.setHandler(ar -> {
            if (ar.succeeded()) {
                ApiContext apiContext1 = ar.result();
                testContext.assertFalse(apiContext1.params().containsKey("sign"));
            } else {
                testContext.fail();
            }
        });
    }

    private String getFirst(Multimap<String, String> params, String paramName) {
        return Lists.newArrayList(params.get(paramName)).get(0);
    }

    private String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
        // 第一步：检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 第二步：把所有参数名和参数值串在一起
        List<String> query = new ArrayList<>(params.size());
        for (String key : keys) {
            String value = getFirst(params, key);
            if (!Strings.isNullOrEmpty(value)) {
                query.add(key + "=" + value);
            }
        }
        String queryString = Joiner.on("&").join(query);
        String sign = null;
        try {
            if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptHmacMd5(queryString, secret);
            } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptHmacSha256(queryString, secret);
            } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptHmacSha512(queryString, secret);
            } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptMD5(secret + queryString + secret);
            }
        } catch (IOException e) {

        }
        return sign;
    }

}
