package com.edgar.direwolves.core.eventbus;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.edgar.util.vertx.eventbus.Event;
import io.vertx.core.eventbus.Message;

/**
 * Created by Edgar on 2017/7/5.
 *
 * @author Edgar  Date 2017/7/5
 */
public class EventbusUtils {

  private EventbusUtils() {
    throw new AssertionError("Not instantiable: " + EventbusUtils.class);
  }

  public static void onFailure(Message<Event> msg, Throwable throwable) {
    if (throwable instanceof SystemException) {
      SystemException ex = (SystemException) throwable;
      msg.fail(ex.getErrorCode().getNumber(), ex.getMessage());
    } else if (throwable instanceof ValidationException) {
      msg.fail(DefaultErrorCode.INVALID_ARGS.getNumber(),
               DefaultErrorCode.INVALID_ARGS.getMessage());
    } else {
      msg.fail(999, throwable.getMessage());
    }
  }
}
