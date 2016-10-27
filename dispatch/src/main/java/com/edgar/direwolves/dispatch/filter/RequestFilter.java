package com.edgar.direwolves.dispatch.filter;

import com.edgar.direwolves.definition.HttpEndpoint;
import com.edgar.direwolves.core.spi.ApiContext;
import com.edgar.direwolves.dispatch.Utils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 将endpoint转换为json对象.
 * params和header的json均为{"k1", ["v1"]}，{"k1", ["v1", "v2]}格式的json对象.
 * <p>
 * <pre>
 *   {
 * "id" : "5bbbe06b-df08-4728-b5e2-166faf912621",
 * "type" : "http",
 * "path" : "/devices",
 * "method" : "POST",
 * "params" : {
 * "q3" : [ "v3" ]
 * },
 * "headers" : {
 * "h3" : [ "v3", "v3.2" ]
 * },
 * "body" : {
 * "foo" : "bar"
 * },
 * "host" : "localhost",
 * "port" : 8080
 * }
 * </pre>
 * <p>
 * Created by edgar on 16-9-20.
 */
public class RequestFilter implements Filter {

  private static final String NAME = "request";

  private Vertx vertx;

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    return true;
//    List<String> filters = apiContext.apiDefinition().filters();
//    return filters.contains(NAME);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    apiContext.apiDefinition().endpoints().stream()
            .filter(e -> e instanceof HttpEndpoint)
            .map(e -> toJson(apiContext, (HttpEndpoint) e))
            .forEach(json -> apiContext.addRequest(json));
    completeFuture.complete(apiContext);
  }

  private JsonObject toJson(ApiContext apiContext, HttpEndpoint endpoint) {
    JsonObject request = new JsonObject();
    request.put("id", UUID.randomUUID().toString());
    request.put("type", "http");
    request.put("path", endpoint.path());
    request.put("method", endpoint.method().name());
    JsonObject params = Utils.mutliMapToJson(apiContext.params());
    request.put("params", params);
    JsonObject headers = Utils.mutliMapToJson(apiContext.headers());
    request.put("headers", headers);
    if (apiContext.body() != null) {
      JsonObject body = apiContext.body().copy();
      request.put("body", body);
    }
    List<Record> records = apiContext.records().stream()
            .filter(r -> endpoint.service().equalsIgnoreCase(r.getName()))
            .collect(Collectors.toList());
    if (records.isEmpty()) {
      throw SystemException.create(DefaultErrorCode.UNKOWN_REMOTE);
    }
    Record record = records.get(0);
    request.put("host",
                record.getLocation().getString("host"));
    request.put("port",
                record.getLocation().getInteger("port"));
    return request;
  }

}