package com.github.edgar615.direwolves.core.eventbus;

import com.github.edgar615.util.exception.ErrorCode;

/**
 * Created by edgar on 17-7-1.
 */
@Deprecated
public class EventbusErrorCode implements ErrorCode {

  private final int number;

  private final int statusCode;

  private final String message;

  public static EventbusErrorCode create(int number, int statusCode, String message) {
    return new EventbusErrorCode(number, statusCode, message);
  }

  public static EventbusErrorCode create(int number, String message) {
    return new EventbusErrorCode(number, 400, message);
  }

  private EventbusErrorCode(int number, int statusCode, String message) {
    this.number = number;
    this.statusCode = statusCode;
    this.message = message;
  }


  @Override
  public int getNumber() {
    return number;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public int getStatusCode() {
    return 0;
  }

  @Override
  public String toString() {
    return "EventbusErrornumber{"
        + "number=" + number
        + ", message='" + message + '\''
        + ", statusCode='" + statusCode + '\''
        + '}';
  }
}
