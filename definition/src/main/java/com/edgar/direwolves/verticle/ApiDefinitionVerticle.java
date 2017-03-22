package com.edgar.direwolves.verticle;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.cmd.ApiCmdFactory;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.definition.ApiProviderImpl;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * java -jar definition-1.0.0.jar run com.edgar.direwolves.definition.DefinitonVerticle.
 * <p>
 * java -jar definition-1.0.0.jar start com.edgar.direwolves.definition.DefinitonVerticle.
 * <p>
 * java -jar definition-1.0.0.jar list
 * <p>
 * java -jar definition-1.0.0.jar stop vertId
 *
 * @author Edgar  Date 2016/9/13
 */
public class ApiDefinitionVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    LOGGER.info("---@ [Definition] [Read config] [OK] [{}]", config().encode());
    registerEventBusConsumer();
    registerBackendApi();

    String namespace = config().getString("project.namespace", "");
    String address = ApiProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    ProxyHelper.registerService(ApiProvider.class, vertx, new ApiProviderImpl(), address);
    LOGGER.info("---@ [Definition] [register ApiProvider] [OK] [{}]", address);
    //读取路由
    if (config().containsKey("api.config.dir")) {
      readApi(startFuture);
    } else {
      startFuture.complete();
    }
  }

  private void readApi(Future<Void> startFuture) {
    String configDir = config().getString("api.config.dir");
    vertx.<List<JsonObject>>executeBlocking(f -> {
      try {
        List<JsonObject> apiList = readApiJsonFromDir(configDir);
        f.complete(apiList);
      } catch (Exception e) {
        LOGGER.error("---@ [Definition] [Read Api] [FAILED] [{}]", e.getMessage());
        f.fail(e);
      }
    }, ar -> {
      if (ar.succeeded()) {
        try {
          List<JsonObject> apiList = ar.result();
          apiList.stream().map(json -> ApiDefinition.fromJson(json))
                  .forEach(d -> ApiDefinitionRegistry.create().add(d));
          startFuture.complete();
        } catch (Exception e) {
          LOGGER.error("---@ [Definition] [Decode Api] [FAILED] [{}]", e.getMessage());
          startFuture.fail(e);
        }
      } else {
        LOGGER.error("---@ [Definition] [Decode Api] [FAILED] [{}]", ar.cause().getMessage());
        startFuture.fail(ar.cause());
      }
    });
  }

  private List<JsonObject> readApiJsonFromDir(String dirPath) throws IOException {
    List<JsonObject> mappings = new ArrayList<>();
    List<File> files = readApiFileFromDir(new File(dirPath));
    ObjectMapper mapper = new ObjectMapper();
    for (File file : files) {
      try {
        Map<String, Object> map = mapper.readValue(file, Map.class);
        mappings.add(new JsonObject(map));
        LOGGER.info("---@ [Definition] [Read Api] [OK] [{}]", file.getAbsolutePath());
      } catch (Exception e) {
        LOGGER.error("---@ [Definition] [Read Api] [FAILED] [{}]", file.getAbsolutePath()
                                                      + ":" + e.getMessage());
      }
    }
    return mappings;
  }

  private List<File> readApiFileFromDir(File jsonDir) {
    Preconditions.checkArgument(jsonDir.isDirectory(),
                                "Api definition directory must be directory");

    List<File> mappingFiles = new ArrayList<>();
    File[] jsonFiles = jsonDir.listFiles((dir, fileName) -> fileName.endsWith(".json"));
    Iterables.addAll(mappingFiles, Arrays.asList(jsonFiles));
    File[] jsonDirs = jsonDir.listFiles((pathname) -> pathname.isDirectory());
    for (File dir : jsonDirs) {
      Iterables.addAll(mappingFiles, readApiFileFromDir(dir));
    }
    return mappingFiles;
  }

  private void registerEventBusConsumer() {
    String namespace = config().getString("project.namespace", "");
    //eventbus consumer
    EventBus eb = vertx.eventBus();
    Lists.newArrayList(ServiceLoader.load(ApiCmdFactory.class))
            .stream()
            .map(f -> f.create(vertx, config()))
            .forEach(cmd -> {
              LOGGER.info("---@ [Definition] [register consumer] [OK] [{}]", cmdAddress(namespace, cmd.cmd()));
              eb.<JsonObject>consumer(cmdAddress(namespace, cmd.cmd()), msg -> {
                Future<JsonObject> future = cmd.handle(msg.body());
                future.setHandler(ar -> {
                  if (ar.succeeded()) {
                    msg.reply(ar.result());
                  } else {
                    eventbusFailureHandler(msg, ar.cause());
                  }
                });
              });
            });
  }

  private String cmdAddress(String namespace, String cmd) {
    if (Strings.isNullOrEmpty(namespace)) {
      return "direwolves.eb." + cmd;
    }
    return namespace + ".direwolves.eb." + cmd;
  }

  private void eventbusFailureHandler(Message<JsonObject> msg, Throwable throwable) {
    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      msg.fail(ex.getErrorCode().getNumber(), ex.getMessage());
    } else if (throwable instanceof ValidationException) {
      msg.fail(DefaultErrorCode.INVALID_ARGS.getNumber(),
               DefaultErrorCode.INVALID_ARGS.getMessage());
    } else {
      msg.fail(999, throwable.getMessage());
    }
  }

  private void registerBackendApi() {
    List<ApiDefinition> apiDefinitions = cmdToBackendApi();
    apiDefinitions.forEach(d -> {
      ApiDefinitionRegistry.create().add(d);
    });
  }

  private List<ApiDefinition> cmdToBackendApi() {
    String namespace = config().getString("project.namespace", "");
    return
            Lists.newArrayList(ServiceLoader.load(ApiCmdFactory.class))
                    .stream()
                    .map(f -> f.create(vertx, config()))
                    .map(cmd -> {
                      String address = cmdAddress(namespace, cmd.cmd());
                      Endpoint endpoint =
                              EventbusEndpoint.reqResp(cmd.cmd(), address, null);
                      ApiDefinition apiDefinition =
                              ApiDefinition.create(address, HttpMethod.GET, "backend/" + cmd.cmd(),
                                                   Lists.newArrayList(endpoint));
                      JsonObject jsonObject = new JsonObject()
                              .put("authentication", true)
                              .put("acl_restriction", new JsonObject()
                                      .put("whitelist", new JsonArray().add("backend"))
                                      .put("blacklist", new JsonArray().add("*")));
                      ApiPlugin.factories.forEach(
                              f -> apiDefinition.addPlugin((ApiPlugin) f.decode(jsonObject)));
                      return apiDefinition;
                    }).collect(Collectors.toList());
  }

}
