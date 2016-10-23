package com.edgar.direwolves.definition.eb;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.ApiDefinitionListCodec;
import com.edgar.direwolves.definition.verticle.ApiDefinitionRegistry;
import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ApiMatchHandler implements EventbusMessageConsumer<JsonObject> {
  public static final String ADDRESS = "api.match";

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
      msg.reply(Lists.newArrayList(definitions),
                new DeliveryOptions().setCodecName
                        (ApiDefinitionListCodec.class.getSimpleName()));
    } catch (Exception e) {
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
