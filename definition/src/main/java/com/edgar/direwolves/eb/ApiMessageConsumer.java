package com.edgar.direwolves.eb;

import com.edgar.direwolves.core.spi.Configurable;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface ApiMessageConsumer<T> extends Configurable, Handler<Message<T>> {

  /**
   * 消息地址
   *
   * @return
   */
  String address();

}
