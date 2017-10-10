package com.github.edgar615.direwolves.plugin.transformer;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/6/22.
 *
 * @author Edgar  Date 2017/6/22
 */
class ResponseTransformerConverter {

  static void fromJson(JsonObject jsonObject, ResponseTransformerPlugin plugin) {
    removeBody(jsonObject, plugin);
    removeHeader(jsonObject, plugin);
    replaceBody(jsonObject, plugin);
    replaceHeader(jsonObject, plugin);
    addBody(jsonObject, plugin);
    addHeader(jsonObject, plugin);
  }

  private static void removeHeader(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray removes = endpoint.getJsonArray("header.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      transformer.removeHeader(removes.getString(j));
    }
  }

  private static void removeBody(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray removes = endpoint.getJsonArray("body.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      transformer.removeBody(removes.getString(j));
    }
  }

  private static void replaceHeader(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray replaces = endpoint.getJsonArray("header.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer
              .replaceHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private static void replaceBody(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray replaces = endpoint.getJsonArray("body.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer
              .replaceBody(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private static void addHeader(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray adds = endpoint.getJsonArray("header.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer.addHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private static void addBody(JsonObject endpoint, ResponseTransformerPlugin transformer) {
    JsonArray adds = endpoint.getJsonArray("body.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      transformer.addBody(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }
}
