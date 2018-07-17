package com.github.edgar615.gateway.core.definition;

import com.github.edgar615.gateway.core.utils.AntPathMatcher;
import io.vertx.core.json.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class ApiDefinitionUtils {
  static boolean match(ApiDefinition definition, JsonObject filter) {
    for (String key : filter.fieldNames()) {
      boolean match;
      switch (key) {
        case "name":
          match = match(definition.name(), filter.getString("name"));
          break;
        case "method":
          match = match(definition.method().name(), filter.getString("method"));
          break;
        case "path":
          match = matchPath(definition, filter.getString("path"));
          break;
        default:
          // metadata
          match = true;
          break;
      }

      if (!match) {
        return false;
      }
    }

    return true;
  }

  static boolean matchPath(ApiDefinition definition, String expected) {
    if (expected.endsWith("/") && expected.length() > 1) {
      expected = expected.substring(0, expected.length() - 1);
    }
    if (definition instanceof AntPathApiDefinition) {
      AntPathMatcher matcher = new AntPathMatcher.Builder().build();
      return !matchIgnore((AntPathApiDefinition) definition, matcher, expected)
              && matcher.isMatch(definition.path(), expected);
    } else if (definition instanceof RegexPathApiDefinition) {
      RegexPathApiDefinition regexPathApiDefinition = (RegexPathApiDefinition) definition;
      Pattern pattern = regexPathApiDefinition.pattern();
      Matcher matcher = pattern.matcher(expected);
      return matcher.matches();
    } else {
      return definition.path().equals(expected);
    }

  }

  static boolean matchIgnore(AntPathApiDefinition definition, AntPathMatcher matcher,
                             String expected) {
    return definition.ignoredPatterns()
            .stream().anyMatch(p -> matcher.isMatch(p, expected));
  }

  static boolean match(Object actual, Object expected) {
    if (actual == null) {
      return false;
    }
    if ("*".equals(expected)) {
      return true;
    }
    if (actual instanceof String) {
      if (((String) actual).equalsIgnoreCase(expected.toString())) {
        return true;
      }
      if (expected.toString().startsWith("*")) {
        return actual.toString().toLowerCase()
                .endsWith(expected.toString().substring(1).toLowerCase());
      }
      if (expected.toString().endsWith("*")) {
        return actual.toString().toLowerCase()
                .startsWith(
                        expected.toString().substring(0, expected.toString().length() - 1)
                                .toLowerCase());
      }
    }
    return actual.equals(expected);
  }
}
