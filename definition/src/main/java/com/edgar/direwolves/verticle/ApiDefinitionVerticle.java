package com.edgar.direwolves.verticle;

import com.edgar.direwolves.eb.ApiDefinitionCodec;
import com.edgar.direwolves.eb.ApiDefinitionListCodec;
import com.edgar.direwolves.eb.ApiMessageConsumer;
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

  @Override
  public void start() throws Exception {
    //eventbus consumer
    Lists.newArrayList(ServiceLoader.load(ApiMessageConsumer.class)).forEach(filter -> filter.config(vertx, new JsonObject()));

    EventBus eb = vertx.eventBus();
    eb.registerCodec(new ApiDefinitionCodec())
        .registerCodec(new ApiDefinitionListCodec());
  }

}
