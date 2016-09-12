package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.ApiDefinition;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 匹配ApiContext和ApiDefinition.
 * 只有当两个对method相同，且ApiContext符合ApiDefinition的正则表达式才认为二者匹配.
 * 如果两者匹配，会将匹配正则对字符串按照$1,$2的顺序设置params.
 * Created by edgar on 16-9-12.
 */
public class ApiMatcher implements BiFunction<ApiContext, ApiDefinition, Boolean> {

    private static final ApiMatcher INSTANCE = new ApiMatcher();

    private ApiMatcher() {

    }

    public static BiFunction<ApiContext, ApiDefinition, Boolean> instance() {
        return INSTANCE;
    }

    @Override
    public Boolean apply(ApiContext apiContext, ApiDefinition apiDefinition) {
        return matches(apiContext, apiDefinition);
    }

    private boolean matches(ApiContext apiContext, ApiDefinition definition) {
        if (apiContext.getMethod() != definition.getMethod()) {
            return false;
        }
        Pattern pattern = definition.getPattern();
        String path = apiContext.getPath();
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            try {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String group = matcher.group(i + 1);
                    if (group != null) {
                        final String k = "param" + (i + 1);
                        final String value = URLDecoder.decode(group, "UTF-8");
                        apiContext.getParams().put(k, value);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                //TODO 异常处理
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
