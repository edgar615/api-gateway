package com.edgar.direwolves.core.utils;

import com.edgar.util.exception.SystemException;
import io.vertx.core.eventbus.Message;

public class EventbusUtils {
  public static void fail(Message msg, Throwable throwable) {
    int failureCode = -1;
    String failureMessage = throwable.getMessage();
    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      failureCode = ex.getErrorCode().getNumber();
      failureMessage = ex.getErrorCode().getMessage();
    }
    msg.fail(failureCode, failureMessage);
  }
}