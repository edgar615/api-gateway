package com.edgar.direwolves.core.utils;

import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2017/7/4.
 *
 * @author Edgar  Date 2017/7/4
 */
public class LoggerUtils {
  private LoggerUtils() {
    throw new AssertionError("Not instantiable: " + LoggerUtils.class);
  }

  public static void trace(Logger logger, String event, String message, Map<String, Object> data) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data));
  }

  public static void trace(Logger logger, String event, String message,
                          String[] keys, Object[] values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void trace(Logger logger, String event, String message,
                          List<String> keys, List<Object> values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void debug(Logger logger, String event, String message, Map<String, Object> data) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data));
  }

  public static void debug(Logger logger, String event, String message,
                          String[] keys, Object[] values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void debug(Logger logger, String event, String message,
                          List<String> keys, List<Object> values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void info(Logger logger, String event, String message) {
    logger.info("[{}] [{}] [{}]",
                event,
                message);
  }

  public static void info(Logger logger, String event, String message, Map<String, Object> data) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data));
  }

  public static void info(Logger logger, String event, String message,
                          String[] keys, Object[] values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void info(Logger logger, String event, String message,
                          List<String> keys, List<Object> values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void warn(Logger logger, String event, String message, Map<String, Object> data) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data));
  }

  public static void warn(Logger logger, String event, String message,
                          String[] keys, Object[] values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void warn(Logger logger, String event, String message,
                          List<String> keys, List<Object> values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void warn(Logger logger, String event, String message,
                          Map<String, Object> data, Throwable throwable) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data),
                throwable);
  }

  public static void warn(Logger logger, String event, String message,
                          String[] keys, Object[] values, Throwable throwable) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values),
                throwable);
  }

  public static void warn(Logger logger, String event, String message,
                          List<String> keys, List<Object> values, Throwable throwable) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values),
                throwable);
  }

  public static void error(Logger logger, String event, String message, Map<String, Object> data) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data));
  }

  public static void error(Logger logger, String event, String message,
                          String[] keys, Object[] values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void error(Logger logger, String event, String message,
                          List<String> keys, List<Object> values) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values));
  }

  public static void error(Logger logger, String event, String message,
                           Map<String, Object> data, Throwable throwable) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(data),
                throwable);
  }

  public static void error(Logger logger, String event, String message,
                           String[] keys, Object[] values,Throwable throwable) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values),
                throwable);
  }

  public static void error(Logger logger, String event, String message,
                           List<String> keys, List<Object> values, Throwable throwable) {
    logger.info("[{}] [{}] [{}]",
                event,
                message,
                dataFormat(keys, values),
                throwable);
  }

  private static String dataFormat(List<String> keys, List<Object> values) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keys.size(); i++) {
      if (i < values.size()) {
        sb.append(keys.get(i))
                .append(":")
                .append(values.get(i))
                .append("; ");
      }
    }
    return sb.toString();
  }

  private static String dataFormat(String[] keys, Object[] values) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keys.length; i++) {
      if (i < values.length) {
        sb.append(keys[i])
                .append(":")
                .append(values[i])
                .append("; ");
      }
    }
    return sb.toString();
  }

  private static String dataFormat(Map<String, Object> data) {
    StringBuilder sb = new StringBuilder();
    for (String field : data.keySet()) {
      sb.append(field)
              .append(":")
              .append(data.get(field))
              .append("; ");
    }
    return sb.toString();
  }

}
