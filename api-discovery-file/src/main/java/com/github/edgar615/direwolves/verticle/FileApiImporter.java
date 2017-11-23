package com.github.edgar615.direwolves.verticle;

import com.github.edgar615.direwolves.core.apidiscovery.ApiImporter;
import com.github.edgar615.direwolves.core.apidiscovery.ApiPublisher;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.log.Log;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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

  private Vertx vertx;

  @Override
  public void start(Vertx vertx, ApiPublisher apiPublisher,
                    JsonObject config, Future<Void> complete) {
    this.vertx = vertx;
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
        List<Future> futures = addApiList(apiPublisher, ar.result());
        Log.create(LOGGER)
                .setEvent("api.import.succeeded")
                .addData("path", path)
                .setMessage("Import api from file")
                .info();

        checkResult(futures, complete);

      } else {
        Log.create(LOGGER)
                .setEvent("api.import.failed")
                .setMessage("Import api from file")
                .setThrowable(ar.cause())
                .error();
        complete.fail(ar.cause());
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
        } else {
          String defineJson = vertx.fileSystem().readFileBlocking(p).toString();
          datas.add(defineJson);
        }
      }
    } else {
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
