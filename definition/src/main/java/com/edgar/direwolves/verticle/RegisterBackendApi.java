package com.edgar.direwolves.verticle;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.cmd.ApiCmdFactory;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.util.vertx.spi.Initializable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ServiceLoader;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class RegisterBackendApi implements Initializable {
  @Override
  public void initialize(Vertx vertx, JsonObject config, Future<Void> complete) {
    String namespace = config.getString("namespace", "");
    Lists.newArrayList(ServiceLoader.load(ApiCmdFactory.class))
            .stream()
            .map(f -> f.create(vertx, config))
            .map(cmd -> cmdToApi(namespace, cmd))
            .forEach(d -> ApiDefinitionRegistry.create().add(d));
    complete.complete();

  }

  private String cmdAddress(String namespace, String cmd) {
    if (Strings.isNullOrEmpty(namespace)) {
      return "direwolves.eb." + cmd;
    }
    return namespace + ".direwolves.eb." + cmd;
  }

  private ApiDefinition cmdToApi(String namespace, ApiCmd cmd) {
    String address = cmdAddress(namespace, cmd.cmd());
    Endpoint endpoint =
            EventbusEndpoint.reqResp(cmd.cmd(), address, null);
    ApiDefinition apiDefinition =
            ApiDefinition.create(address, HttpMethod.POST, "backend/" + cmd.cmd(),
                                 Lists.newArrayList(endpoint));
    JsonObject jsonObject = new JsonObject()
            .put("strict_arg", false)
            .put("authentication", true)
            .put("acl_restriction", new JsonObject()
                    .put("whitelist", new JsonArray().add("backend"))
                    .put("blacklist", new JsonArray().add("*")));

    ApiPlugin.factories.forEach(
            f -> apiDefinition.addPlugin((ApiPlugin) f.decode(jsonObject)));
    return apiDefinition;
  }
}
