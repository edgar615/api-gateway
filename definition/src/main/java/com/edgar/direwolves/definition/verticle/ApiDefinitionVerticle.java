package com.edgar.direwolves.definition.verticle;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import com.edgar.direwolves.definition.ApiDefinitionCodec;
import com.edgar.direwolves.definition.ApiDefinitionListCodec;
import com.google.common.collect.Lists;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.ServiceLoader;

/**
 * java -jar definition-1.0.0.jar run com.edgar.direwolves.definition.DefinitonVerticle.
 * <p/>
 * java -jar definition-1.0.0.jar start com.edgar.direwolves.definition.DefinitonVerticle.
 * <p/>
 * java -jar definition-1.0.0.jar list
 * <p/>
 * java -jar definition-1.0.0.jar stop vertId
 *
 * @author Edgar  Date 2016/9/13
 */
public class ApiDefinitionVerticle extends AbstractVerticle {

  public static final String API_DELETE_RATE_LIMIT = "api.ratelimit.delete";

  public final JsonObject RESULT_OK = new JsonObject().put("result", "OK");

  @Override
  public void start() throws Exception {

    //eventbus consumer
    Lists.newArrayList(ServiceLoader.load(EventbusMessageConsumer.class))
            .forEach(filter -> filter.config(vertx, new JsonObject()));

    EventBus eb = vertx.eventBus();
    eb.registerCodec(new ApiDefinitionCodec())
            .registerCodec(new ApiDefinitionListCodec());
  }
}
