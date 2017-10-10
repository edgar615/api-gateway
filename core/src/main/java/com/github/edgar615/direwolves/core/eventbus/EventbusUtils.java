package com.github.edgar615.direwolves.core.eventbus;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import com.github.edgar615.util.vertx.eventbus.Event;
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
