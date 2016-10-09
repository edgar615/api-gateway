package com.edgar.direwolves.definition;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * Created by edgar on 16-9-24.
 */
class HttpEndpointDecoder implements Function<JsonObject, HttpEndpoint> {

  private static final HttpEndpointDecoder INSTANCE = new HttpEndpointDecoder();

  private HttpEndpointDecoder() {
  }

  public static Function<JsonObject, HttpEndpoint> instance() {
    return INSTANCE;
  }


  @Override
  public HttpEndpoint apply(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    String name = jsonObject.getString("name");
    Preconditions.checkNotNull(name, "arg name cannot be null");
    String service = jsonObject.getString("service");
    Preconditions.checkNotNull(service, "arg service cannot be null");
    String path = jsonObject.getString("path");
    Preconditions.checkNotNull(path, "arg path cannot be null");
    if ("http".equalsIgnoreCase(type)) {
      HttpMethod method = HttpMethodDecoder.instance().apply(jsonObject);
      HttpEndpoint httpEndpoint = Endpoint.createHttp(name, method, path, service);
      removeReqHeader(jsonObject, httpEndpoint);
      removeReqUrlArg(jsonObject, httpEndpoint);
      removeReqBodyArg(jsonObject, httpEndpoint);
      replaceReqHeader(jsonObject, httpEndpoint);
      replaceReqUrlArg(jsonObject, httpEndpoint);
      replaceReqBodyArg(jsonObject, httpEndpoint);
      addReqHeader(jsonObject, httpEndpoint);
      addReqUrlArg(jsonObject, httpEndpoint);
      addReqBodyArg(jsonObject, httpEndpoint);
      return httpEndpoint;
    } else {
      throw new UnsupportedOperationException("unsupport type:" + type);
    }
  }

  private void removeReqHeader(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray removes = endpoint.getJsonArray("request.header.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      httpEndpoint.removeReqHeader(removes.getString(j));
    }
  }

  private void removeReqUrlArg(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray removes = endpoint.getJsonArray("request.query.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      httpEndpoint.removeReqUrlArg(removes.getString(j));
    }
  }

  private void removeReqBodyArg(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray removes = endpoint.getJsonArray("request.body.remove", new JsonArray());
    for (int j = 0; j < removes.size(); j++) {
      httpEndpoint.removeReqBodyArg(removes.getString(j));
    }
  }

  private void replaceReqHeader(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray replaces = endpoint.getJsonArray("request.header.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      httpEndpoint
              .replaceReqHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void replaceReqUrlArg(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray replaces = endpoint.getJsonArray("request.query.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      httpEndpoint
              .replaceReqUrlArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void replaceReqBodyArg(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray replaces = endpoint.getJsonArray("request.body.replace", new JsonArray());
    for (int j = 0; j < replaces.size(); j++) {
      String value = replaces.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      httpEndpoint
              .replaceReqBodyArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void addReqHeader(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray adds = endpoint.getJsonArray("request.header.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      httpEndpoint.addReqHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void addReqUrlArg(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray adds = endpoint.getJsonArray("request.query.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      httpEndpoint.addReqUrlArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

  private void addReqBodyArg(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    JsonArray adds = endpoint.getJsonArray("request.body.add", new JsonArray());
    for (int j = 0; j < adds.size(); j++) {
      String value = adds.getString(j);
      Iterable<String> iterable =
              Splitter.on(":").omitEmptyStrings().trimResults().split(value);
      httpEndpoint.addReqBodyArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
    }
  }

}
