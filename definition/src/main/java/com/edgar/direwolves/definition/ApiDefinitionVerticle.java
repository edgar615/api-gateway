package com.edgar.direwolves.definition;

import com.google.common.base.Strings;
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

    public static final String API_ADD_BLACK = "api.blacklist.add";

    public static final String API_ADD_WHITE = "api.whitelist.add";

    public static final String API_DELETE_BLACK = "api.blacklist.delete";

    public static final String API_DELETE_WHITE = "api.whitelist.delete";

    public static final String API_ADD_RATE_LIMIT = "api.ratelimit.add";

    public static final String API_DELETE_RATE_LIMIT = "api.ratelimit.delete";

    public static final String API_ADD_AUTH = "api.auth.add";

    public static final String API_DELETE_AUTH = "api.auth.delete";

    public final JsonObject RESULT_OK = new JsonObject().put("result", "OK");

    @Override
    public void start() throws Exception {
        EventBus eb = vertx.eventBus();
        eb.<JsonObject>consumer(API_ADD, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                ApiDefinition apiDefinition = JsonToApiDefinition.instance().apply(jsonObject);
                if (apiDefinition != null) {
                    ApiDefinitionRegistry.create().add(apiDefinition);
                }

                List<AuthDefinition> authDefinitions = JsonToAuthDefinition.instance().apply(jsonObject);
                if (!authDefinitions.isEmpty()) {
                    authDefinitions.forEach(authDefinition -> AuthDefinitionRegistry.create().add(authDefinition));
                }

                List<RateLimitDefinition> rateLimitDefinitions = JsonToRateLimitDefinition.instance().apply(jsonObject);
                if (!rateLimitDefinitions.isEmpty()) {
                    rateLimitDefinitions.forEach(rateLimitDefinition -> RateLimitDefinitionRegistry.create().add(rateLimitDefinition));
                }

                IpRestrictionDefinition ipRestrictionDefinition = JsonToIpRestrictionDefinition.instance().apply(jsonObject);
                if (ipRestrictionDefinition != null) {
                    IpRestrictionDefinitionRegistry registry = IpRestrictionDefinitionRegistry.create();
                    ipRestrictionDefinition.whitelist().forEach(ip -> registry.addWhitelist(ipRestrictionDefinition.apiName(), ip));
                    ipRestrictionDefinition.blacklist().forEach(ip -> registry.addBlacklist(ipRestrictionDefinition.apiName(), ip));
                }

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

        eb.<JsonObject>consumer(API_GET, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                String name = jsonObject.getString("name");
                List<ApiDefinition> definitions = ApiDefinitionRegistry.create().filter(name);
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
                ApiDefinitionRegistry.create().remove(name);
                AuthDefinitionRegistry.create().remove(name, null);
                IpRestrictionDefinitionRegistry.create().remove(name);
                RateLimitDefinitionRegistry.create().remove(name, null, null);
                msg.reply(RESULT_OK);
            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });

        eb.<JsonObject>consumer(API_ADD_AUTH, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                String name = jsonObject.getString("name");
                String type = jsonObject.getString("type");

                AuthType authType = AuthType.valueOf(type.toUpperCase());
                AuthDefinitionRegistry.create().add(AuthDefinition.create(name, authType));
                msg.reply(RESULT_OK);

            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });

        eb.<JsonObject>consumer(API_DELETE_AUTH, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                String name = jsonObject.getString("name", null);
                String type = jsonObject.getString("type", null);

                if (Strings.isNullOrEmpty(type)) {
                    AuthDefinitionRegistry.create().remove(name, null);
                } else {
                    AuthType authType = AuthType.valueOf(type.toUpperCase());
                    AuthDefinitionRegistry.create().remove(name, authType);
                    msg.reply(RESULT_OK);
                }

            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });

        eb.<JsonObject>consumer(API_ADD_BLACK, msg -> {
            try {
                JsonObject jsonObject = msg.body();
                String name = jsonObject.getString("name");
                String ip = jsonObject.getString("ip");
                IpRestrictionDefinitionRegistry.create().addBlacklist(name, ip);
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
                IpRestrictionDefinitionRegistry.create().addWhitelist(name, ip);
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
                IpRestrictionDefinitionRegistry.create().removeBlacklist(name, ip);
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
                IpRestrictionDefinitionRegistry.create().removeWhitelist(name, ip);
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

                RateLimitType rateLimitType = RateLimitType.valueOf(type.toUpperCase());
                RateLimitBy rateLimitBy = RateLimitBy.valueOf(limitBy.toUpperCase());

                RateLimitDefinitionRegistry.create().add(RateLimitDefinition.create(name, rateLimitBy, rateLimitType, limit));
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

                RateLimitType rateLimitType = null;
                RateLimitBy rateLimitBy = null;
                if (!Strings.isNullOrEmpty(type)) {
                    rateLimitType = RateLimitType.valueOf(type.toUpperCase());
                }

                if (!Strings.isNullOrEmpty(limitBy)) {
                    rateLimitBy = RateLimitBy.valueOf(limitBy.toUpperCase());
                }

                RateLimitDefinitionRegistry.create().remove(name, rateLimitBy, rateLimitType);
                msg.reply(RESULT_OK);

            } catch (Exception e) {
                msg.fail(-1, e.getMessage());
            }
        });
    }
}
