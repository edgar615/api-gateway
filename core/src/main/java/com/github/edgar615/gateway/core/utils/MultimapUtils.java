package com.github.edgar615.gateway.core.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Optional;

/**
 * Multimap的工具类.
 *
 * @author Edgar  Date 2017/1/5
 */
public class MultimapUtils {

    private MultimapUtils() {
        throw new AssertionError("Not instantiable: " + MultimapUtils.class);
    }

    /**
     * 获取Multimap中的第一个参数.
     *
     * @param multimap 参数列表
     * @param key      参数名 忽略大小写
     * @return 参数值
     */
    public static String getCaseInsensitive(Multimap<String, String> multimap, String key) {
        Optional<String> optional = multimap.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(key))
                .findFirst();
        if (!optional.isPresent()) {
            return null;
        }
        List<String> values = Lists.newArrayList(multimap.get(optional.get()));
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 获取Multimap中的第一个参数.
     *
     * @param multimap 参数列表
     * @param key      参数名
     * @return 参数值
     */
    public static String getFirst(Multimap<String, String> multimap, String key) {
        List<String> values = Lists.newArrayList(multimap.get(key));
        if (values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 将Multimap转换为字符串用来记录日志.
     *
     * @param multimap      Multimap
     * @param defaultString 如果Multimap是空，返回的默认字符串
     * @return 字符串
     */
    public static String convertToString(Multimap<String, String> multimap, String defaultString) {
        StringBuilder s = new StringBuilder();
        for (String key : multimap.keySet()) {
            s.append(key)
                    .append(":")
                    .append(Joiner.on(",").join(multimap.get(key)))
                    .append(";");
        }
        if (s.length() == 0) {
            return defaultString;
        }
        return s.toString();
    }
}
