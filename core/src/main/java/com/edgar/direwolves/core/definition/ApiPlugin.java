package com.edgar.direwolves.core.definition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public interface ApiPlugin {
  List<ApiPluginFactory> factories = Lists.newArrayList(
          ServiceLoader.load(ApiPluginFactory.class));

  /**
   * @return 插件名称
   */
  String name();

  static ApiPlugin create(String name) {
    return factory(name).create();
  }

  static ApiPluginFactory factory(String name) {
    Preconditions.checkNotNull(name, "name cannot null");
    List<ApiPluginFactory> apiPluginFactories =
            factories.stream().filter(f -> name.equalsIgnoreCase(f.name()))
                    .collect(Collectors.toList());
    if (apiPluginFactories.isEmpty()) {
      throw new NoSuchElementException("no such factory->" + name);
    }
    return apiPluginFactories.get(0);
  }

  default JsonObject encode() {
    return factories.stream().filter(f -> this.name().equalsIgnoreCase(f.name()))
            .map(f -> f.encode(this))
            .findFirst().orElseGet(() -> new JsonObject());
//    return factory(this.name()).encode(this);
  }

}
