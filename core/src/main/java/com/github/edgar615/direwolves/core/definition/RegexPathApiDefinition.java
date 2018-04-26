package com.github.edgar615.direwolves.core.definition;

import com.github.edgar615.util.base.MorePreconditions;
import com.google.common.base.Preconditions;
import io.vertx.core.http.HttpMethod;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 正则匹配规则的API定义
 */
public class RegexPathApiDefinition extends ApiDefinitionImpl {
  /**
   * 路径的正则表达式.在目前的设计中，它和path保持一致.
   */
  private final Pattern pattern;

  RegexPathApiDefinition(String name, HttpMethod method, String path, List<Endpoint> endpoints) {
    super(name, method, path, endpoints);
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    this.pattern = Pattern.compile(path);
  }
  /**
   * @return 路径的正则表达式.
   */
  public Pattern pattern() {
    return pattern;
  }
}
