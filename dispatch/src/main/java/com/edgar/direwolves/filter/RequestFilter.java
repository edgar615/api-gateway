package com.edgar.direwolves.filter;

import com.edgar.direwolves.definition.HttpEndpoint;
import com.edgar.direwolves.dispatch.ApiContext;
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
 * 转换为rpc调用的过滤器.
 * <p>
 * </pre>
 * <p>
 * Created by edgar on 16-9-20.
 */
public class RequestFilter implements Filter {

  private static final String NAME = "request_transfomer";

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
    List<String> filters = apiContext.apiDefinition().filters();
    return filters.contains(NAME);
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
    String newPath = Uitls.replaceUrl(endpoint.path(), apiContext);
    request.put("path", newPath);
    request.put("method", endpoint.method().name());
    JsonObject params = Uitls.mutliMapToJson(apiContext.params());
    request.put("params", params);
    JsonObject headers = Uitls.mutliMapToJson(apiContext.headers());
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
