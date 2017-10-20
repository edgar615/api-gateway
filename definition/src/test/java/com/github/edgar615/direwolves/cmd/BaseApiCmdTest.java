package com.github.edgar615.direwolves.cmd;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.direwolves.core.cmd.CmdRegister;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.awaitility.Awaitility;
import org.junit.Before;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/7/5.
 *
 * @author Edgar  Date 2017/7/5
 */
public class BaseApiCmdTest {
  protected Vertx vertx;

  protected ApiDiscovery discovery;

  protected String namespace;
  //  ApiCmd cmd;
  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    discovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace + ".api"));
    Future<Void> importCmdFuture = Future.future();
    new CmdRegister().initialize(vertx, new JsonObject(), importCmdFuture);
//    cmd = new AddApiCmdFactory().create(vertx, new JsonObject());
  }

  protected void addMockApi() {
    AddApiCmd addApiCmd = new AddApiCmd(vertx, new JsonObject());
    JsonObject jsonObject = new JsonObject()
            .put("name", "add_device")
            .put("method", "POST")
            .put("path", "/devices");
    JsonArray endpoints = new JsonArray()
            .add(new JsonObject().put("type", "simple-http").put("host", "localhost").put("port", 80)
                         .put("name", "add_device")
                         .put("service", "device")
                         .put("method", "POST")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    AtomicBoolean check1 = new AtomicBoolean();
    addApiCmd.handle(new JsonObject().put("namespace", namespace).put("data", jsonObject.encode()))
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check1.set(true);
              } else {
                ar.cause().printStackTrace();
              }
            });
    Awaitility.await().until(() -> check1.get());

    jsonObject = new JsonObject()
            .put("name", "get_device")
            .put("method", "GET")
            .put("path", "/devices");
    endpoints = new JsonArray()
            .add(new JsonObject().put("type", "simple-http").put("host", "localhost").put("port", 80)
                         .put("name", "get_device")
                         .put("service", "device")
                         .put("method", "GET")
                         .put("path", "/devices"));
    jsonObject.put("endpoints", endpoints);

    AtomicBoolean check2 = new AtomicBoolean();
    addApiCmd.handle(new JsonObject().put("namespace", namespace).put("data", jsonObject.encode()))
            .setHandler(ar -> {
              if (ar.succeeded()) {
                check2.set(true);
              } else {
                ar.cause().printStackTrace();
              }
            });
    Awaitility.await().until(() -> check2.get());
  }
}
