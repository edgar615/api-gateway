package com.edgar.direwolves.core.utils;

import com.google.common.base.Strings;

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

  /**
   * 应用
   */
  private String application = "api-gateway";

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

  public void trace() {
    try {
      if (logger.isTraceEnabled()) {
        LogData logData = new LogData().get();
        if (throwable != null) {
          logger.trace(logData.getLogFormat(), logData.getLogArgs().toArray(), throwable);
        } else {
          logger.trace(logData.getLogFormat(), logData.getLogArgs().toArray());
        }
      }
    } catch (Exception e) {
      defaultLogger.error("log error", e);
    }
  }

  public void debug() {
    try {
      if (logger.isDebugEnabled()) {
        LogData logData = new LogData().get();
        if (throwable != null) {
          logger.debug(logData.getLogFormat(), logData.getLogArgs().toArray(), throwable);
        } else {
          logger.debug(logData.getLogFormat(), logData.getLogArgs().toArray());
        }
      }
    } catch (Exception e) {
      defaultLogger.error("log error", e);
    }
  }

  public void info() {
    try {
      if (logger.isInfoEnabled()) {
        LogData logData = new LogData().get();
        if (throwable != null) {
          logger.info(logData.getLogFormat(), logData.getLogArgs().toArray(), throwable);
        } else {
          logger.info(logData.getLogFormat(), logData.getLogArgs().toArray());
        }
      }
    } catch (Exception e) {
      defaultLogger.error("log error", e);
    }
  }

  public void warn() {
    try {
      if (logger.isWarnEnabled()) {
        LogData logData = new LogData().get();
        if (throwable != null) {
          logger.warn(logData.getLogFormat(), logData.getLogArgs().toArray(), throwable);
        } else {
          logger.warn(logData.getLogFormat(), logData.getLogArgs().toArray());
        }
      }
    } catch (Exception e) {
      defaultLogger.error("log error", e);
    }
  }

  public void error() {
    try {
      if (logger.isErrorEnabled()) {
        LogData logData = new LogData().get();
        if (throwable != null) {
          logger.error(logData.getLogFormat(), logData.getLogArgs().toArray(), throwable);
        } else {
          logger.error(logData.getLogFormat(), logData.getLogArgs().toArray());
        }
      }
    } catch (Exception e) {
      defaultLogger.error("log error", e);
    }
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

  public Log addData(String key, Object data) {
    this.data.put(key, data);
    return this;
  }

  public Log addDatas(Map<String, Object> data) {
    this.data.putAll(data);
    return this;
  }

  public Log addArg(Object arg) {
    this.args.add(arg);
    return this;
  }

  private class LogData {
    private String logFormat;

    private List<Object> logArgs;

    public String getLogFormat() {
      return logFormat;
    }

    public List<Object> getLogArgs() {
      return logArgs;
    }

    public LogData get() {
      logFormat = "[{},{}] [{},{}]";
      logArgs = new ArrayList<>();
      logArgs.add(application);
      if (traceId == null) {
        logArgs.add("");
      } else {
        logArgs.add(traceId);
      }
      logArgs.add(module);
      logArgs.add(event);

      if (Strings.isNullOrEmpty(message)) {
        logFormat += " [no msg]";
      } else {
        logFormat += " [" + message + "]";
        logArgs.addAll(args);
      }

      if (!data.isEmpty()) {
        logFormat += " [{}]";
        logArgs.add(dataFormat(data));
      }
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
}
