package com.github.edgar615.gateway.dispatch;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.json.JsonArray;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edgar on 2016/10/13.
 *
 * @author Edgar  Date 2016/10/13
 */
public class UrlTest {

    @Test
    public void test() {
        System.out.println(new JsonArray().add("1").add(
                "$header.h1").encode());
        System.out.println(
                matchValue("devices/new/$param.param0/test/$param.param1", "[\\w./$]*([\\w$"
                                                                           + ".]+)"));
        String url = "devices/new/$param.param0/test/$param.param0";
        Pattern pattern = Pattern.compile("[\\w./]+([\\w$.]+)[\\w./]*");
        Matcher matcher = pattern.matcher(url);
        System.out.println(matcher.matches());
        if (matcher.matches()) {
            if (matcher.groupCount() > 0) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String group = matcher.group(i + 1);
                    if (group != null) {
                        final String k = "param" + i;
                        final String value =
                                QueryStringDecoder.decodeComponent(group.replace("+", "%2b"));
                        System.out.println(value);
                    }
                }
            }
        }
    }

    private List<String> matchValue(String baseString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(baseString);
        List<String> matchValues = new ArrayList<>();
        if (matcher.matches()) {
            if (matcher.groupCount() > 0) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String group = matcher.group(i + 1);
                    if (group != null) {
                        final String value =
                                QueryStringDecoder.decodeComponent(group.replace("+", "%2b"));
                        matchValues.add(value);
                    }
                }
            }
        }
        return matchValues;
    }
}
