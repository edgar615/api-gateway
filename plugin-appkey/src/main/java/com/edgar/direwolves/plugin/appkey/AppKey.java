package com.edgar.direwolves.plugin.appkey;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/20.
 *
 * @author Edgar  Date 2017/7/20
 */
public class AppKey {

  private final String appkey;

  private final JsonObject jsonObject;

  public AppKey(String appkey, JsonObject jsonObject) {
    this.appkey = appkey;
    this.jsonObject = jsonObject;
  }

  public String getAppkey() {
    return appkey;
  }

  public JsonObject getJsonObject() {
    return jsonObject;
  }
}
