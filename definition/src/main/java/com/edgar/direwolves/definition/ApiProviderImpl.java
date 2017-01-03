package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
                Future.failedFuture(SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)));
      }
    } catch (Exception e) {
      LOGGER.error("failed match api, method->{}, path->{}", method, path);
      handler.handle(
              Future.failedFuture(SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)));
    }
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
