package com.github.edgar615.direwolves.core.definition;

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
          match = matchPath(definition.pattern(), filter.getString("path"));
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

  static boolean matchPath(Pattern pattern, String expected) {
    if (expected.endsWith("/") && expected.length() > 1) {
      expected = expected.substring(0, expected.length() - 1);
    }
    Matcher matcher = pattern.matcher(expected);
    return matcher.matches();
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
