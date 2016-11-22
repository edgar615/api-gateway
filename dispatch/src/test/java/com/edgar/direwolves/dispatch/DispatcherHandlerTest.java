package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.utils.EventbusUtils;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/11/16.
 *
 * @author Edgar  Date 2016/11/16
 */
@RunWith(VertxUnitRunner.class)
public class DispatcherHandlerTest {

  Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    ApiDefinitionRegistry apiDefinitionRegistry = ApiDefinitionRegistry.create();
    ApiDefinition definition =
        ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/minimize.json"));
    apiDefinitionRegistry.add(definition);
    vertx.deployVerticle(ApiDispatchVerticle.class.getName(), testContext.asyncAssertSuccess());
    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), testContext.asyncAssertSuccess());

    vertx.eventBus().<JsonObject>consumer("direwolves.rpc.http.req", msg -> {
      JsonObject jsonObject = msg.body();
      try {
        msg.reply(jsonObject.copy());
      } catch (Exception e) {
        EventbusUtils.fail(msg, e);
      }
    });

    vertx.eventBus().<JsonObject>consumer("service.discovery.select", msg -> {
      try {
        Record record = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
        msg.reply(record.toJson());
      } catch (Exception e) {
        EventbusUtils.fail(msg, e);
      }
    });
  }

  @Test
  public void testDispatch(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient().post(8080, "localhost", "/devices", response -> {
      response.bodyHandler(body -> {
        System.out.println(body.toString());
        async.complete();
      });
    }).setChunked(true).end(new JsonObject().put("username", "edgar").encode());
  }
}
