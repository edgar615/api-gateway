package com.edgar.direwolves.definition;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Edgar on 2016/9/13.
 *
 * @author Edgar  Date 2016/9/13
 */
public class JsonUtils {

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
