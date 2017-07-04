package com.edgar.direwolves.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2017/7/4.
 *
 * @author Edgar  Date 2017/7/4
 */
public class Log {

  private final Logger defaultLogger = LoggerFactory.getLogger("root");

  /**
   * 数据
   */
  private final Map<String, Object> data = new HashMap<>();

  private final List<Object> args = new ArrayList<>();

  private Logger logger = defaultLogger;

  private LogLevel level = LogLevel.INFO;

  /**
   * 模块
   */
  private String application;

  /**
   * 方法或者事件
   */
  private String event;

  /**
   * 模块，或者类
   */
  private String module;

  /**
   * 简要描述
   */
  private String message;

  /**
   * 跟踪ID
   */
  private String traceId;

  /**
   * 异常
   */
  private Throwable throwable;

  private Log(Logger logger) {
    this.logger = logger;
  }

  public static Log create(Logger logger) {
    return new Log(logger);
  }

  /**
   * 日志格式为 [{应用，traceId}] [模块，方法（或者事件）]
   */
  public void ouput() {
    try {
      String logFormat = "[{},{}] [{},{}] " + message;
      List<Object> logArgs = new ArrayList<>();
      logArgs.add(application);
      logArgs.add(traceId);
      logArgs.add(module);
      logArgs.add(event);
      logArgs.addAll(args);
      if (level == LogLevel.TRACE) {
        if (throwable != null) {
          logger.trace(logFormat, logArgs.toArray(), throwable);
        } else {
          logger.trace(logFormat, logArgs.toArray());
        }
      } else if (level == LogLevel.DEBUG) {
        if (throwable != null) {
          logger.debug(logFormat, logArgs.toArray(), throwable);
        } else {
          logger.debug(logFormat, logArgs.toArray());
        }
      } else if (level == LogLevel.INFO) {
        if (throwable != null) {
          logger.info(logFormat, logArgs.toArray(), throwable);
        } else {
          logger.info(logFormat, logArgs.toArray());
        }
      } else if (level == LogLevel.WARN) {
        if (throwable != null) {
          logger.warn(logFormat, logArgs.toArray(), throwable);
        } else {
          logger.warn(logFormat, logArgs.toArray());
        }
      } else if (level == LogLevel.ERROR) {
        if (throwable != null) {
          logger.error(logFormat, logArgs.toArray(), throwable);
        } else {
          logger.error(logFormat, logArgs.toArray());
        }
      }


    } catch (Exception e) {
      defaultLogger.error("log error", e);
    }
  }

  public Log setLevel(LogLevel level) {
    this.level = level;
    return this;
  }

  public Log setApplication(String application) {
    this.application = application;
    return this;
  }

  public Log setEvent(String event) {
    this.event = event;
    return this;
  }

  public Log setModule(String module) {
    this.module = module;
    return this;
  }

  public Log setMessage(String message) {
    this.message = message;
    return this;
  }

  public Log setThrowable(Throwable throwable) {
    this.throwable = throwable;
    return this;
  }

  public Log setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public Log addArg(Object arg) {
    this.args.add(arg);
    return this;
  }

  private String dataFormat(Map<String, Object> data) {
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
