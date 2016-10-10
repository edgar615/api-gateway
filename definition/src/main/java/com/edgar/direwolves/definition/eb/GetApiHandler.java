package com.edgar.direwolves.definition.eb;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.ApiDefinitionCodec;
import com.edgar.direwolves.definition.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class GetApiHandler implements EventbusMessageConsumer<String> {
  public static final String ADDRESS = "api.get";

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
        msg.fail(404, "no result");
      } else {
        msg.reply(definitions.get(0), new DeliveryOptions().setCodecName
                (ApiDefinitionCodec.class.getSimpleName()));
      }
    } catch (Exception e) {
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }
}
