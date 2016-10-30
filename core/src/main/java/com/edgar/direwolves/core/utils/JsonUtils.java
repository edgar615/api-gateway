package com.edgar.direwolves.core.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.MultiMap;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonUtils {

  /**
   * 将Multimap转换为JsonObject参数.
   *
   * JsonObject的值均为JsonArray
   *
   * @param map Multimap
   * @return JsonObject
   */
  public static JsonObject mutlimapToJson(Multimap<String, String> map) {
    JsonObject jsonObject = new JsonObject();
    map.asMap().forEach((key, values) -> {
      if (values != null) {
        jsonObject.put(key, Lists.newArrayList(values));
//        if (values.size() > 1) {
//          jsonObject.put(key, Lists.newArrayList(values));
//        } else {
//          jsonObject.put(key, Iterables.get(values, 0));
//        }
      }
    });
    return jsonObject;
  }

    public static JsonObject getJsonFromFile(String jsonFile) {
        JsonObject conf;
        if (jsonFile != null) {
            try (Scanner scanner = new Scanner(new File(jsonFile)).useDelimiter("\\A")) {
                String sconf = scanner.next();
                try {
                    conf = new JsonObject(sconf);
                } catch (DecodeException e) {
//                    log.error("Configuration file " + sconf + " does not contain a valid JSON object");
                    return null;
                }
            } catch (FileNotFoundException e) {
                try {
                    conf = new JsonObject(jsonFile);
                } catch (DecodeException e2) {
                    // The configuration is not printed for security purpose, it can contain sensitive data.
//                    log.error("The -conf option does not point to an existing file or is not a valid JSON object");
                    e2.printStackTrace();
                    return null;
                }
            }
        } else {
            conf = null;
        }
        return conf;
    }
}
