package com.edgar.direwolves.definition;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

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

    public final JsonObject RESULT_OK = new JsonObject().put("result", "OK");

    @Override
    public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        eb.<JsonObject>consumer(API_ADD, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                ApiDefinition apiDefinition = JsonToApiDefinition.instance().apply(jsonObject);
                ApiDefinitionRegistryImpl.instance().add(apiDefinition);
                msg.reply(RESULT_OK);
            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });

        eb.<JsonObject>consumer(API_LIST, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                Integer start = jsonObject.getInteger("start", 0);
                Integer limit = jsonObject.getInteger("limit", 10);
                JsonArray apiArray = new JsonArray();
                List<ApiDefinition> definitions = ApiDefinitionRegistryImpl.instance().filter(null);
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

        eb.<JsonObject>consumer(API_GET, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                String name = jsonObject.getString("name");
                List<ApiDefinition> definitions = ApiDefinitionRegistryImpl.instance().filter(name);
                if (definitions.isEmpty()) {
                    msg.fail(404, "no result");
                } else {
                    msg.reply(ApiDefinitionToJson.instance().apply(definitions.get(0)));
                }
            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });

        eb.<JsonObject>consumer(API_DELETE, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                String name = jsonObject.getString("name");
                ApiDefinitionRegistryImpl.instance().remove(name);
                msg.reply(RESULT_OK);
            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });
    }
}
