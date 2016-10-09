package com.edgar.direwolves.definition;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ListApiHandler implements EventbusMessageConsumer<JsonObject> {
  public static final String ADDRESS = "api.list";

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(ADDRESS, this::handle);
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject jsonObject = msg.body();
      Integer start = jsonObject.getInteger("start", 0);
      Integer limit = jsonObject.getInteger("limit", 10);
      String name = jsonObject.getString("name", null);
      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
      int toIndex = start + limit;
      if (toIndex > definitions.size()) {
        toIndex = definitions.size();
      }
      msg.reply(Lists.newArrayList(definitions.subList(start, toIndex)), new DeliveryOptions().setCodecName
              (ApiDefinitionListCodec.class.getSimpleName()));
    } catch (Exception e) {
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }
}
