package com.edgar.direwolves.eb;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ApiGetHandler implements ApiMessageConsumer<String> {
  public static final String ADDRESS = "eb.api.get";
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiGetHandler.class);

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(ADDRESS, this::handle);
  }

  @Override
  public void handle(Message<String> msg) {
    try {
      String name = msg.body();
      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
      if (definitions.isEmpty()) {
        LOGGER.error("no such api, name->{}", name);
        msg.fail(404, "no result");
      } else {
        LOGGER.error("get api, name->{}", name);
        msg.reply(definitions.get(0), new DeliveryOptions().setCodecName
            (ApiDefinitionCodec.class.getSimpleName()));
      }
    } catch (Exception e) {
      LOGGER.error("failed get api, error->{}", e.getMessage(), e);
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }
}
