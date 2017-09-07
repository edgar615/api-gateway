package com.edgar.direwolves.mock;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/9/7.
 *
 * @author Edgar  Date 2017/9/7
 */
@Deprecated
public class Device {
  private final int id;

  private final String name;

  public Device(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public JsonObject toJson() {
    return new JsonObject().put("id", id).put("name", name);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
