package com.edgar.direwolves.dispatch;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 将vertx的MultiMap转换为JsonObject
 *
 * @author Edgar  Date 2016/8/24
 */
public class MultiMapToJsonObject implements Function<MultiMap, JsonObject> {

  private static final MultiMapToJsonObject INSTANCE = new MultiMapToJsonObject();

  private MultiMapToJsonObject() {

  }

  public static Function<MultiMap, JsonObject> instance() {
    return INSTANCE;
  }

  @Override
  public JsonObject apply(MultiMap multiMap) {
    JsonObject jsonObject = new JsonObject();
    Set<String> names = multiMap.names();
    for (String paramName : names) {
      List<String> values = multiMap.getAll(paramName);
      if (values.size() == 1) {
        jsonObject.put(paramName, values.get(0));
      } else {
        jsonObject.put(paramName, values);
      }
    }
    return jsonObject;
  }
}