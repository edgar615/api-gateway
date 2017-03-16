package com.edgar.direwolves.core.utils;

import org.slf4j.Logger;

/**
 * Created by Edgar on 2017/3/15.
 *
 * @author Edgar  Date 2017/3/15
 */
public class Helper {

  /**
   * 记录方法运行过程中的成功日志
   * ---| [id] [OK] [方法] [描述] [参数（可选值）]
   *
   * @param logger Logger
   * @param id     跟踪ID
   * @param method 方法
   * @param desc   描述
   */
  public static void logOK(Logger logger, String id, String method, String desc) {
    logger.info("---| [{}] [OK] [{}] [{}]",
                id,
                method,
                desc);
  }

  /**
   * 记录方法运行过程中的失败日志
   * ---| [id] [FAILED] [方法] [描述] [参数（可选值）]
   *
   * @param logger Logger
   * @param id     跟踪ID
   * @param method 方法
   * @param desc   描述
   */
  public static void logFailed(Logger logger, String id, String method, String desc) {
    logger.warn("---| [{}] [FAILED] [{}] [{}]",
                id,
                method,
                desc);
  }

  /**
   * 记录方法运行过程中的异常日志
   * ---| [id] [FAILED] [方法] [描述] [参数（可选值）]
   *
   * @param logger    Logger
   * @param id        跟踪ID
   * @param method    方法
   * @param desc      描述
   * @param throwable 异常
   */
  public static void logError(Logger logger, String id, String method, String desc,
                              Throwable throwable) {
    logger.error("---| [{}] [FAILED] [{}] [{}]",
                 id,
                 method,
                 desc,
                 throwable);
  }
}
