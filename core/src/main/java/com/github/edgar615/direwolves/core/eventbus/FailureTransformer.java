package com.github.edgar615.direwolves.core.eventbus;

import com.github.edgar615.util.exception.CustomErrorCode;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.ErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * 异常的处理类.
 *
 * @author Edgar  Date 2016/2/18
 */
@Deprecated
public class FailureTransformer implements Function<Throwable, SystemException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FailureTransformer.class);

  public static Function<Throwable, SystemException> create() {
    return new FailureTransformer();
  }

  @Override
  public SystemException apply(Throwable throwable) {
    if (throwable instanceof SystemException) {
      return (SystemException) throwable;
    } else if (throwable instanceof ValidationException) {
      SystemException ex = SystemException.create(DefaultErrorCode.INVALID_ARGS);
      ValidationException vex = (ValidationException) throwable;
      if (!vex.getErrorDetail().isEmpty()) {
        ex.set("details", vex.getErrorDetail().asMap());
      }
      return ex;
    } else if (throwable instanceof ReplyException) {
      ReplyException replyException = (ReplyException) throwable;
      if (replyException.failureType() == ReplyFailure.NO_HANDLERS) {
        return SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
            .set("details", replyException.getMessage());
      } else if (replyException.failureType() == ReplyFailure.TIMEOUT) {
        return SystemException.create(DefaultErrorCode.TIME_OUT)
            .set("details", replyException.getMessage());
      } else if (replyException.failureType() == ReplyFailure.RECIPIENT_FAILURE) {
        ErrorCode errorCode = DefaultErrorCode.getCode(replyException.failureCode());
        if (errorCode == null) {
          errorCode = CustomErrorCode.create(replyException.failureCode(), replyException
                  .getMessage(), 400);
        }
        return SystemException.create(errorCode);
      } else {
        ErrorCode errorCode = CustomErrorCode.create(replyException.failureCode(), replyException
                .getMessage(),400);
        return SystemException.create(errorCode);
      }
    } else {
      return SystemException.wrap(DefaultErrorCode.UNKOWN, throwable);
    }

  }

}