//package com.edgar.direwolves.definition.eb;
//
//import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
//import com.edgar.direwolves.core.spi.ApiDefinition;
//import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
//import io.vertx.core.Vertx;
//import io.vertx.core.eventbus.EventBus;
//import io.vertx.core.eventbus.Message;
//import io.vertx.core.json.JsonObject;
//
//import java.util.List;
//
///**
// * Created by Edgar on 2016/10/8.
// *
// * @author Edgar  Date 2016/10/8
// */
//public class DeleteWhitelistHandler implements EventbusMessageConsumer<JsonObject> {
//  public static final String ADDRESS = "api.whitelist.delete";
//
//  @Override
//  public void config(Vertx vertx, JsonObject config) {
//    EventBus eb = vertx.eventBus();
//    eb.consumer(ADDRESS, this::handle);
//  }
//
//  @Override
//  public void handle(Message<JsonObject> msg) {
//    try {
//      JsonObject jsonObject = msg.body();
//      String name = jsonObject.getString("name", null);
//      String ip = jsonObject.getString("ip");
//      List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
//      if (ip == null) {
//        definitions.forEach(definition -> definition.removeAllWhitelist());
//      } else {
//        definitions.forEach(definition -> definition.removeWhitelist(ip));
//      }
//      msg.reply(new JsonObject().put("result", "OK"));
//    } catch (Exception e) {
//      msg.fail(-1, e.getMessage());
//    }
//  }
//
//  @Override
//  public String address() {
//    return ADDRESS;
//  }
//}
