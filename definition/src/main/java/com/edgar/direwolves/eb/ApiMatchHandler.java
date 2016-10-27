package com.edgar.direwolves.eb;

import com.edgar.direwolves.core.spi.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ApiMatchHandler implements ApiMessageConsumer<JsonObject> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiMatchHandler.class);
  public static final String ADDRESS = "eb.api.match";

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(ADDRESS, this::handle);
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject jsonObject = msg.body();
      HttpMethod method = method(jsonObject.getString("method", "GET"));
      String path = jsonObject.getString("path", "/");
      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().match(method, path);
      LOGGER.error("match api, method->{}, path->{}", method, path);
      msg.reply(Lists.newArrayList(definitions),
                new DeliveryOptions().setCodecName
                        (ApiDefinitionListCodec.class.getSimpleName()));
    } catch (Exception e) {
      LOGGER.error("failed match api, params->{}", msg.body());
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
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
