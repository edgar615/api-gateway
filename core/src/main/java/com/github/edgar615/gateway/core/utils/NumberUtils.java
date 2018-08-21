package com.github.edgar615.gateway.core.utils;

/**
 * Created by Edgar on 2018/1/31.
 *
 * @author Edgar  Date 2018/1/31
 */
public class NumberUtils {
    public static Number tryParse(String source) {
        if (source == null) {
            return null;
        }
        Integer intValue = toInt(source);
        if (intValue != null) {
            return intValue;
        }
        Long longValue = toLong(source);
        if (longValue != null) {
            return longValue;
        }
        Float floatValue = toFloat(source);
        if (floatValue != null) {
            return floatValue;
        }
        Double doubleValue = toDouble(source);
        if (doubleValue != null) {
            return doubleValue;
        }
        return null;
    }

    public static Integer toInt(final String str) {
        if (str == null) {
            return null;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return null;
        }
    }

    public static Long toLong(final String str) {
        if (str == null) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return null;
        }
    }

    public static Float toFloat(final String str) {
        if (str == null) {
            return null;
        }
        try {
            return Float.parseFloat(str);
        } catch (final NumberFormatException nfe) {
            return null;
        }
    }

    public static Double toDouble(final String str) {
        if (str == null) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (final NumberFormatException nfe) {
            return null;
        }
    }

}
