package com.github.edgar615.direwolves.core.eventbus;

import com.github.edgar615.util.exception.ErrorCode;

/**
 * Created by edgar on 17-7-1.
 */
public class EventbusErrorCode implements ErrorCode {

  private final int number;

  private final int statusCode;

  private final String message;

  private EventbusErrorCode(int number, String message, int statusCode) {
    this.number = number;
    this.statusCode = statusCode;
    this.message = message;
  }

  public static EventbusErrorCode create(int number, String message, int statusCode) {
    return new EventbusErrorCode(number, message,statusCode);
  }

  public static EventbusErrorCode create(int number, String message) {
    return new EventbusErrorCode(number, message, 400);
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
    return "EventbusErrorCode{"
           + "number=" + number
           + ", message='" + message + '\''
           + ", statusCode='" + statusCode + '\''
           + '}';
  }
}
