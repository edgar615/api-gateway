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
      HttpEndpoint httpEndpoint = Endpoint.createHttp(name, method, path, service, null);
      removeTransformer(jsonObject, httpEndpoint);
      replaceTransformer(jsonObject, httpEndpoint);
      return httpEndpoint;
    } else {
      throw new UnsupportedOperationException("unsupport " + type);
    }
  }


  private void removeTransformer(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    if (endpoint.containsKey("request_transformer")) {
      JsonObject transfromer = endpoint.getJsonObject("request_transformer");
      if (transfromer.containsKey("remove")) {
        JsonObject remove = transfromer.getJsonObject("remove");
        if (remove.containsKey("headers")) {
          JsonArray removes = remove.getJsonArray("headers");
          for (int j = 0; j < removes.size(); j++) {
            httpEndpoint.removeHeader(removes.getString(j));
          }
        }
        if (remove.containsKey("url_args")) {
          JsonArray removes = remove.getJsonArray("url_args");
          for (int j = 0; j < removes.size(); j++) {
            httpEndpoint.removeUrlArg(removes.getString(j));
          }
        }
        if (remove.containsKey("body_args")) {
          JsonArray removes = remove.getJsonArray("body_args");
          for (int j = 0; j < removes.size(); j++) {
            httpEndpoint.removeBodyArg(removes.getString(j));
          }
        }
      }
    }
  }

  private void replaceTransformer(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    if (endpoint.containsKey("request_transformer")) {
      JsonObject transfromer = endpoint.getJsonObject("request_transformer");
      if (transfromer.containsKey("replace")) {
        JsonObject replace = transfromer.getJsonObject("replace");
        if (replace.containsKey("headers")) {
          JsonArray replaces = replace.getJsonArray("headers");
          for (int j = 0; j < replaces.size(); j++) {
            String value = replaces.getString(j);
            Iterable<String> iterable =
                    Splitter.on(":").omitEmptyStrings().trimResults().split(value);
            httpEndpoint
                    .replaceRequestHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
          }
        }
        if (replace.containsKey("url_args")) {
          JsonArray replaces = replace.getJsonArray("url_args");
          for (int j = 0; j < replaces.size(); j++) {
            String value = replaces.getString(j);
            Iterable<String> iterable =
                    Splitter.on(":").omitEmptyStrings().trimResults().split(value);
            httpEndpoint
                    .replaceRequestUrlArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
          }
        }
        if (replace.containsKey("body_args")) {
          JsonArray replaces = replace.getJsonArray("body_args");
          for (int j = 0; j < replaces.size(); j++) {
            String value = replaces.getString(j);
            Iterable<String> iterable =
                    Splitter.on(":").omitEmptyStrings().trimResults().split(value);
            httpEndpoint
                    .replaceRequestBodyArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
          }
        }
      }
    }
  }

  private void addTransformer(JsonObject endpoint, HttpEndpoint httpEndpoint) {
    if (endpoint.containsKey("request_transformer")) {
      JsonObject transfromer = endpoint.getJsonObject("request_transformer");
      if (transfromer.containsKey("add")) {
        JsonObject add = transfromer.getJsonObject("add");
        if (add.containsKey("headers")) {
          JsonArray adds = add.getJsonArray("headers");
          for (int j = 0; j < adds.size(); j++) {
            String value = adds.getString(j);
            Iterable<String> iterable =
                    Splitter.on(":").omitEmptyStrings().trimResults().split(value);
            httpEndpoint.addRequestHeader(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
          }
        }
        if (add.containsKey("url_args")) {
          JsonArray adds = add.getJsonArray("url_args");
          for (int j = 0; j < adds.size(); j++) {
            String value = adds.getString(j);
            Iterable<String> iterable =
                    Splitter.on(":").omitEmptyStrings().trimResults().split(value);
            httpEndpoint.addRequestUrlArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
          }
        }
        if (add.containsKey("body_args")) {
          JsonArray adds = add.getJsonArray("body_args");
          for (int j = 0; j < adds.size(); j++) {
            String value = adds.getString(j);
            Iterable<String> iterable =
                    Splitter.on(":").omitEmptyStrings().trimResults().split(value);
            httpEndpoint.addRequestBodyArg(Iterables.get(iterable, 0), Iterables.get(iterable, 1));
          }
        }
      }
    }
  }
}
