package com.github.edgar615.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by edgar on 17-3-10.
 */
public abstract class RequestReplaceFilter  implements Filter {
  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE - 1;
  }

  protected Multimap<String, String> replaceHeader(ApiContext apiContext,
                                                   Multimap<String, String> headers) {
    Multimap<String, String> newHeaders = ArrayListMultimap.create();
    for (String key : headers.keySet()) {
      List<String> values = new ArrayList<>(headers.get(key));
      for (String val : values) {
        Object newVal = apiContext.getValueByKeyword(val);
        if (newVal != null) {
          if (newVal instanceof List) {
            List valList = (List) newVal;
            for (int i = 0; i < valList.size(); i++) {
              newHeaders.put(key, getNewVal(apiContext, valList.get(i)).toString());
            }
          } else if (newVal instanceof JsonArray) {
            JsonArray valList = (JsonArray) newVal;
//            newHeaders.putAll(key, valList.getList());
            for (int i = 0; i < valList.size(); i++) {
              newHeaders.put(key, getNewVal(apiContext, valList.getValue(i)).toString());
            }
          } else if (newVal instanceof JsonObject) {
            JsonObject newJsonObject = replaceBody(apiContext, (JsonObject) newVal);
            newHeaders.put(key, newJsonObject.encode());
          } else if (newVal instanceof Map) {
            JsonObject newJsonObject =
                    replaceBody(apiContext, new JsonObject((Map<String, Object>) newVal));
            newHeaders.put(key, newJsonObject.encode());
          } else {
            newHeaders.put(key, newVal.toString());
          }
        }
      }
    }
    return newHeaders;

  }

  protected JsonObject replaceBody(ApiContext apiContext, JsonObject body) {
    JsonObject newBody = new JsonObject();
    if (body != null) {
      for (String key : body.fieldNames()) {
        Object newVal = getNewVal(apiContext, body.getValue(key));
        if (newVal != null) {
          newBody.put(key, newVal);
        }
      }
    }
    return newBody;
  }

  private Object getNewVal(ApiContext apiContext, Object value) {
    if (value instanceof String) {
      String val = (String) value;
      return apiContext.getValueByKeyword(val);
    } else if (value instanceof JsonArray) {
      JsonArray val = (JsonArray) value;
      JsonArray replacedArray = new JsonArray();
      for (int i = 0; i < val.size(); i++) {
        Object newVal = getNewVal(apiContext, val.getValue(i));
        if (newVal != null) {
          replacedArray.add(newVal);
        }
      }
      return replacedArray.isEmpty() ? null : replacedArray;
    } else if (value instanceof JsonObject) {
      JsonObject val = (JsonObject) value;
      JsonObject replacedObject = new JsonObject();
      for (String key : val.fieldNames()) {
        Object newVal = getNewVal(apiContext, val.getValue(key));
        if (newVal != null) {
          replacedObject.put(key, newVal);
        }
      }
      return replacedObject.isEmpty() ? null : replacedObject;
    } else {
      return value;
    }
  }
}
