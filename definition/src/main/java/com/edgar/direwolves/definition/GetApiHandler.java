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
public class GetApiHandler implements EventbusMessageConsumer<JsonObject> {
  public static final String API_GET = "api.get";

  public final JsonObject RESULT_OK = new JsonObject().put("result", "OK");

  @Override
  public void config(Vertx vertx, JsonObject config) {
    EventBus eb = vertx.eventBus();
    eb.consumer(API_GET, this::handle);
  }

  @Override
  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject jsonObject = msg.body();
      String name = jsonObject.getString("name");
      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
      if (definitions.isEmpty()) {
        msg.fail(404, "no result");
      } else {
        msg.reply(definitions.get(0));
      }
    } catch (Exception e) {
      msg.fail(-1, e.getMessage());
    }
  }

  @Override
  public String address() {
    return API_GET;
  }
}
