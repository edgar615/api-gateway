package com.github.edgar615.direwolves.filter;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-9-20.
 */
public class ResponseReplaceFilterFactory implements FilterFactory {


  @Override
  public String name() {
    return ResponseReplaceFilter.class.getSimpleName();
  }

  @Override
  public Filter create(Vertx vertx, JsonObject config) {
    return new ResponseReplaceFilter();
  }
}