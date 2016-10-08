package com.edgar.direwolves.definition;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.spi.EventbusMessageConsumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
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

  public static final String API_LIST = "api.list";

  public static final String API_ADD = "api.add";

  public static final String API_GET = "api.get";

  public static final String API_DELETE = "api.delete";

  public static final String API_ADD_BLACK = "api.blacklist.add";

  public static final String API_ADD_WHITE = "api.whitelist.add";

  public static final String API_DELETE_BLACK = "api.blacklist.delete";

  public static final String API_DELETE_WHITE = "api.whitelist.delete";

  public static final String API_ADD_RATE_LIMIT = "api.ratelimit.add";

  public static final String API_DELETE_RATE_LIMIT = "api.ratelimit.delete";

  public static final String API_ADD_FILTER = "api.filter.add";

  public static final String API_DELETE_FILTER = "api.filter.delete";

  public final JsonObject RESULT_OK = new JsonObject().put("result", "OK");

  @Override
  public void start() throws Exception {
    Lists.newArrayList(ServiceLoader.load(EventbusMessageConsumer.class))
            .forEach(filter -> filter.config(vertx, new JsonObject()));

    EventBus eb = vertx.eventBus();
    eb.<JsonObject>consumer(API_LIST, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        Integer start = jsonObject.getInteger("start", 0);
        Integer limit = jsonObject.getInteger("limit", 10);
        JsonArray apiArray = new JsonArray();
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(null);
        if (start <= definitions.size()) {
          limit = limit < definitions.size() - start ? limit : definitions.size() - start;
        }
        definitions.subList(start, limit)
                .forEach(definition -> apiArray.add(
                        new JsonObject()
                                .put("name", definition.name())
                                .put("method", definition.method())
                                .put("path", definition.path())
                                .put("scope", definition.scope())
                ));
        msg.reply(apiArray);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_DELETE, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        ApiDefinitionRegistry.create().remove(name);
        msg.reply(RESULT_OK);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_ADD_FILTER, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String filter = jsonObject.getString("filter");
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.addFilter(filter));
        msg.reply(RESULT_OK);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_DELETE_FILTER, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name", null);
        String filter = jsonObject.getString("filter");
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.removeFilter(filter));
        msg.reply(RESULT_OK);

      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_ADD_BLACK, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String ip = jsonObject.getString("ip");
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.addBlacklist(ip));
        msg.reply(RESULT_OK);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_ADD_WHITE, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String ip = jsonObject.getString("ip");
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.addWhitelist(ip));
        msg.reply(RESULT_OK);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_DELETE_BLACK, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String ip = jsonObject.getString("ip");
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.removeBlacklist(ip));
        msg.reply(RESULT_OK);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_DELETE_WHITE, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String ip = jsonObject.getString("ip");
        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.removeWhitelist(ip));
        msg.reply(RESULT_OK);
      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_ADD_RATE_LIMIT, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String type = jsonObject.getString("type");
        int limit = jsonObject.getInteger("limit");
        String limitBy = jsonObject.getString("limit_by");
        RateLimit rateLimit = RateLimit.create(limitBy, type, limit);

        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.addRateLimit(rateLimit));
        msg.reply(RESULT_OK);

      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });

    eb.<JsonObject>consumer(API_DELETE_RATE_LIMIT, msg -> {
      try {
        JsonObject jsonObject = msg.body();
        String name = jsonObject.getString("name");
        String type = jsonObject.getString("type", null);
        String limitBy = jsonObject.getString("limit_by", null);

        List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
        definitions.forEach(definition -> definition.removeRateLimit(limitBy, type));
        msg.reply(RESULT_OK);

      } catch (Exception e) {
        msg.fail(-1, e.getMessage());
      }
    });
  }
}
