package com.github.edgar615.direwolves.plugin.appkey.discovery;

import com.google.common.base.MoreObjects;
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("AppKey")
        .add("appkey", appkey)
        .add("json", jsonObject.encode())
        .toString();
  }
}
