package com.edgar.direwolves.core.eventbus;

import com.edgar.util.event.Event;
import com.edgar.util.event.EventImpl;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/6/30.
 *
 * @author Edgar  Date 2017/6/30
 */
public class EventImplCodec implements MessageCodec<EventImpl, EventImpl> {
  @Override
  public void encodeToWire(Buffer buffer, EventImpl event) {
    buffer.appendString(new JsonObject(event.toMap()).encode());
  }

  @Override
  public EventImpl decodeFromWire(int pos, Buffer buffer) {
    System.out.println(buffer.toString());
    int length = buffer.getInt(pos);
    pos += 4;
    byte[] encoded = buffer.getBytes(pos, pos + length);
    JsonObject jsonObject = Buffer.buffer(encoded).toJsonObject();
    return (EventImpl) Event.fromMap(jsonObject.getMap());
  }

  @Override
  public EventImpl transform(EventImpl event) {
    return (EventImpl) Event.fromMap(event.toMap());
  }

  @Override
  public String name() {
    return EventImplCodec.class.getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
