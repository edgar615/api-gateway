package com.edgar.direwolves.plugin.arg;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-10-23.
 */
class ArgPluginImpl implements ArgPlugin {
  /**
   * 参数
   */
  private final List<Parameter> parameters = new ArrayList<>();

  ArgPluginImpl() {
  }

  @Override
  public List<Parameter> parameters() {
    return ImmutableList.copyOf(parameters);
  }

  @Override
  public ArgPlugin add(Parameter parameter) {
    parameters.add(parameter);
    return this;
  }

  @Override
  public ArgPlugin remove(String name) {
    Parameter parameter = parameter(name);
    if (parameter != null) {
      parameters.remove(parameter);
    }
    return this;
  }

  @Override
  public Parameter parameter(String name) {
    Preconditions.checkNotNull(name, "name cannot be null");
    List<Parameter> list = parameters.stream()
            .filter(p -> name.equalsIgnoreCase(p.name()))
            .collect(Collectors.toList());
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public ArgPlugin clear() {
    parameters.clear();
    return this;
  }

}
