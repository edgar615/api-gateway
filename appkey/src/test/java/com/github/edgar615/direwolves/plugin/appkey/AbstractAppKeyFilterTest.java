package com.github.edgar615.direwolves.plugin.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.base.EncryptUtils;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by edgar on 16-10-31.
 */
public abstract class AbstractAppKeyFilterTest {

  protected ApiContext createContext(String appKey, String signMethod) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", appKey);
    params.put("nonce", Randoms.randomAlphabetAndNum(10));
    params.put("signMethod", signMethod);
    params.put("v", "1.0");
    params.put("sign", Randoms.randomAlphabetAndNum(16).toUpperCase());

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                    80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppKeyPlugin.class.getSimpleName()));
    return apiContext;
  }

  private String getFirst(Multimap<String, String> params, String paramName) {
    return Lists.newArrayList(params.get(paramName)).get(0);
  }

  protected String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
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
