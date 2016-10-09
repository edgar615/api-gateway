package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class DeleteFilterHandler implements EventbusMessageConsumer<JsonObject> {
  public static final String ADDRESS = "api.filter.delete";

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(ADDRESS, this::handle);
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject jsonObject = msg.body();
      String name = jsonObject.getString("name");
      String filter = jsonObject.getString("filter");
      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
      if (filter == null) {
        definitions.forEach(definition -> definition.removeAllFilter());
      } else {
        definitions.forEach(definition -> definition.removeFilter(filter));
      }
      msg.reply(new JsonObject().put("result", "OK"));

    } catch (Exception e) {
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return ADDRESS;
  }
}
