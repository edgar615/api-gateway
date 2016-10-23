package com.edgar.direwolves.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public interface ApiPlugin {
  List<ApiPluginFactory> factories = Lists.newArrayList(
      ServiceLoader.load(ApiPluginFactory.class));

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

  /**
   * @return 插件名称
   */
  String name();

  default JsonObject encode() {
    return factory(this.name()).encode(this);
  }

}
