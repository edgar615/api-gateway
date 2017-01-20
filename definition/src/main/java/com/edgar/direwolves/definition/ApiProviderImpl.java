package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.exception.DefaultErrorCode;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
public class ApiProviderImpl implements ApiProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiProviderImpl.class);

  @Override
  public void match(String method, String path, Handler<AsyncResult<JsonObject>> handler) {
    try {
      HttpMethod httpMethod = method(method);
      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().match(httpMethod, path);
      if (definitions.size() == 1) {
        LOGGER.debug("match api, method->{}, path->{}", method, path);
        ApiDefinition apiDefinition = definitions.get(0);
        JsonObject jsonObject = apiDefinition.toJson();
        handler.handle(Future.succeededFuture(jsonObject));
      } else {
        LOGGER.error("failed match api, method->{}, path->{}", method, path);
        handler.handle(
                Future.failedFuture(new ServiceException(DefaultErrorCode.RESOURCE_NOT_FOUND
                                                                 .getNumber(), DefaultErrorCode
                                                                 .RESOURCE_NOT_FOUND
                                                                 .getMessage())));
      }
    } catch (Exception e) {
      LOGGER.error("failed match api, method->{}, path->{}", method, path);
      handler.handle(
              Future.failedFuture(new ServiceException(DefaultErrorCode.RESOURCE_NOT_FOUND
                                                               .getNumber(), DefaultErrorCode
                                                               .RESOURCE_NOT_FOUND.getMessage())));
    }
  }

  @Override
  public void list(String name, Handler<AsyncResult<List<JsonObject>>> handler) {
    List<JsonObject> results = ApiDefinitionRegistry.create().filter(name)
            .stream().map(d -> d.toJson())
            .collect(Collectors.toList());
    handler.handle(Future.succeededFuture(results));
  }

  @Override
  public void addPlugin(String name, JsonObject pluginJson,
                        Handler<AsyncResult<JsonObject>> handler) {
    ApiDefinitionRegistry.create().filter(name).
            forEach(d ->
                            ApiPlugin.factories
                                    .forEach(f -> d.addPlugin((ApiPlugin) f.decode(pluginJson)))
            );
    handler.handle(Future.succeededFuture(new JsonObject().put("result", 1)));
  }

  private HttpMethod method(String method) {
    HttpMethod httpMethod = HttpMethod.GET;
    if ("GET".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.GET;
    }
    if ("DELETE".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.DELETE;
    }
    if ("POST".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.POST;
    }
    if ("PUT".equalsIgnoreCase(method)) {
      httpMethod = HttpMethod.PUT;
    }
    return httpMethod;
  }
}
