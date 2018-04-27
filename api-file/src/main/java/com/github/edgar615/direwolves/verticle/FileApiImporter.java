package com.github.edgar615.direwolves.verticle;

import com.github.edgar615.direwolves.core.apidiscovery.ApiImporter;
import com.github.edgar615.direwolves.core.apidiscovery.ApiPublisher;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.utils.Log;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 从文件中读取Api.
 *
 * @author Edgar  Date 2017/3/30
 */
class FileApiImporter implements ApiImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileApiImporter.class);

  private static final String APPLICATION = FileApiDiscoveryVerticle.class.getSimpleName();

  private final List<String> imported = new ArrayList<>();

  private Vertx vertx;

  private ApiPublisher publisher;

  private JsonObject config;

  @Override
  public void start(Vertx vertx, ApiPublisher publisher,
                    JsonObject config, Future<Void> complete) {
    this.vertx = vertx;
    this.publisher = publisher;
    this.config = config;
    doImport(complete);
  }

  public void doImport(Future<Void> complete) {
    String path = config.getString("path");
    vertx.<List<String>>executeBlocking(f -> {
      try {
        List<String> apiList = readFromFile(path);
        f.complete(apiList);
      } catch (Exception e) {
        f.fail(e);
      }
    }, ar -> {
      if (ar.succeeded()) {
        List<Future> futures = addApiList(publisher, ar.result());
        Log.create(LOGGER)
                .setApplication(APPLICATION)
                .setEvent("import")
                .addData("path", path)
                .info();
        checkResult(futures, complete);

      } else {
        Log.create(LOGGER)
                .setApplication(APPLICATION)
                .setEvent("import")
                .addData("path", path)
                .setThrowable(ar.cause())
                .error();
        complete.complete();
      }
    });
  }

  @Override
  public void restart(Future<Void> complete) {
    close(v -> {
      doImport(complete);
    });
  }

  @Override
  public void close(Handler<Void> closeHandler) {
    if (publisher == null) {
      closeHandler.handle(null);
      return;
    }
    // Remove all the services that has been imported
    List<Future> list = new ArrayList<>();
    imported.forEach(name -> {
      Future<Void> fut = Future.future();
      fut.setHandler(ar -> {
        LOGGER.info("Unregistering " + name);
        if (ar.succeeded()) {
          list.add(Future.succeededFuture());
        } else {
          list.add(Future.failedFuture(ar.cause()));
        }
      });
      publisher.unpublish(name, fut.completer());
    });

    CompositeFuture.all(list).setHandler(ar -> {
      imported.clear();
      if (ar.succeeded()) {
        LOGGER.info("Successfully closed the file importer " + this);
      } else {
        LOGGER.error("A failure has been caught while stopping " + this, ar.cause());
      }
      if (closeHandler != null) {
        closeHandler.handle(null);
      }
    });
  }

  private void checkResult(List<Future> futures, Future<Void> complete) {
    CompositeFuture.all(futures)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                complete.complete();
                return;
              }
              complete.fail(ar.cause());
            });
  }

  private List<Future> addApiList(ApiPublisher publisher, List<String> apiList) {
    List<Future> futures = new ArrayList<Future>();
    for (String str : apiList) {
      try {
        ApiDefinition d = ApiDefinition.fromJson(new JsonObject(str));
        Future<ApiDefinition> addFuture = addApi(publisher, d);
        futures.add(addFuture);
      } catch (Exception e) {
        LOGGER.error("[api.imported] [{}]", str, e);
      }
    }
    return futures;
  }

  private Future<ApiDefinition> addApi(ApiPublisher publisher, ApiDefinition definition) {
    Future<ApiDefinition> future = Future.future();
    publisher.publish(definition, ar -> {
      if (ar.failed()) {
        future.fail(ar.cause());
        return;
      }
      imported.add(definition.name());
      future.complete(ar.result());
    });
    return future;
  }

  private List<String> readFromFile(String path) {
    List<String> datas = new ArrayList<>();
    if (Files.isDirectory(new File(path).toPath())) {
      List<String> paths = vertx.fileSystem().readDirBlocking(path);
      for (String p : paths) {
        if (Files.isDirectory(new File(p).toPath())) {
          datas.addAll(readFromFile(p));
        } else if (p.endsWith(".json")) {
          String defineJson = vertx.fileSystem().readFileBlocking(p).toString();
          datas.add(defineJson);
        }
      }
    } else if (path.endsWith(".json")) {
      Buffer buffer = vertx.fileSystem().readFileBlocking(path);
      try {
        String defineJson = buffer.toString();
        datas.add(defineJson);
      } catch (Exception e) {
        LOGGER.error("[file.readed] [FAILED] [{}]", path
                + ":" + e.getMessage());
      }
    }
    return datas;
  }
}
