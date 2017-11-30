package com.github.edgar615.direwolves.standalone;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import com.github.edgar615.util.base.EncryptUtils;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Edgar on 2016/8/25.
 *
 * @author Edgar  Date 2016/8/25
 */
public class Api {

  private String token;

  private String path;

  private Map<String, Object> header = new HashMap<>();

  private Map<String, Object> params;

  private JsonObject body;

  private String appKey = "RmOI7jCvDtfZ1RcAkea1";

  private String appSecret = "dbb0f95c8ebf4317942d9f5057d0b38e";

  private String signMethod = EncryptUtils.HMACMD5;

  public static Api create() {
    return new Api();
  }

  public String path() {
    return path;
  }

  public Api setPath(String path) {
    this.path = path;
    return this;
  }

  public Map<String, Object> params() {
    return params;
  }

  public Api addParam(Map<String, Object> params) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.putAll(params);
    return this;
  }

  public Api addParam(String name, Object value) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.put(name, value);
    return this;
  }


  public Api addHeader(String name, Object value) {
    if (this.header == null) {
      this.header = new HashMap<>();
    }
    this.header.put(name, value);
    return this;
  }


  public Api addBody(JsonObject body) {
    if (this.body == null) {
      this.body = body;
    } else {
      this.body.mergeIn(body);
    }
    return this;
  }

  public Api addBody(String name, Object value) {
    if (this.body == null) {
      this.body = new JsonObject();
    }
    this.body.put(name, value);
    return this;
  }

  public JsonObject getBody() {
    return body;
  }

  public String setAppKey() {
    return appKey;
  }

  public Api setAppKey(String appKey) {
    this.appKey = appKey;
    return this;
  }

  public String setAppSecret() {
    return appSecret;
  }

  public Api setAppSecret(String appSecret) {
    this.appSecret = appSecret;
    return this;
  }

  public String setSignMethod() {
    return signMethod;
  }

  public Api setSignMethod(String signMethod) {
    this.signMethod = signMethod;
    return this;
  }

  public String setToken() {
    return token;
  }

  public Api setToken(String token) {
    this.token = token;
    return this;
  }

  public String signTopRequest() {
    if (params == null) {
      params = new HashMap<>();
    }
    params.putIfAbsent("appKey", appKey);
    params.putIfAbsent("timestamp", Instant.now().getEpochSecond());
    params.putIfAbsent("signMethod", signMethod);
    params.putIfAbsent("nonce", UUID.randomUUID().toString());

    if (body != null) {
      params.put("body", body.encode());
    }

    // 第一步：检查参数是否已经排序
    String[] keys = params.keySet().toArray(new String[0]);
    Arrays.sort(keys);

    // 第二步：把所有参数名和参数值串在一起
    List<String> query = new ArrayList<>(params.size());
    List<String> base = new ArrayList<>(params.size());
    for (String key : keys) {
      String value = params.get(key).toString();
      if (!Strings.isNullOrEmpty(value)) {
        query.add(key + "=" + value);
      }
      if (!"body".equals(key)) {
        base.add(key + "=" + value);
      }
    }
    String queryString = Joiner.on("&").join(query);
    String sign = null;
    try {
      if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacMd5(queryString, appSecret);
      } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha256(queryString, appSecret);
      } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha512(queryString, appSecret);
      } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptMD5(appSecret + queryString + appSecret);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    System.out.println(queryString);
    System.out.println(Joiner.on("&").join(base) + "&sign=" + sign);
    return Joiner.on("&").join(base) + "&sign=" + sign;
  }

}
