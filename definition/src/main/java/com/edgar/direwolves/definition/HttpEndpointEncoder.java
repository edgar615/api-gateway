package com.edgar.direwolves.definition;

import io.vertx.core.json.JsonObject;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
class HttpEndpointEncoder implements Function<HttpEndpoint, JsonObject> {
  private static final HttpEndpointEncoder INSTANCE = new HttpEndpointEncoder();

  private HttpEndpointEncoder() {
  }

  static Function<HttpEndpoint, JsonObject> instance() {
    return INSTANCE;
  }

  @Override
  public JsonObject apply(HttpEndpoint httpEndpoint) {
    return new JsonObject()
            .put("type", httpEndpoint.type())
            .put("name", httpEndpoint.name())
            .put("service", httpEndpoint.service())
            .put("path", httpEndpoint.path())
            .put("method", httpEndpoint.method())
            .put("request.header.remove", httpEndpoint.reqHeadersRemove())
            .put("request.query.remove", httpEndpoint.reqUrlArgsRemove())
            .put("request.body.remove", httpEndpoint.reqBodyArgsRemove())
            .put("request.header.replace", httpEndpoint.reqHeadersReplace()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("request.query.replace", httpEndpoint.reqUrlArgsReplace()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("request.body.replace", httpEndpoint.reqBodyArgsReplace()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("request.header.add", httpEndpoint.reqHeadersAdd()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("request.query.add", httpEndpoint.reqUrlArgsAdd()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()))
            .put("request.body.add", httpEndpoint.reqBodyArgsAdd()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.toList()));
  }
}
