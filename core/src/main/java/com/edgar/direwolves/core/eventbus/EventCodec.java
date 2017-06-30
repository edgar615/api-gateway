package com.edgar.direwolves.core.eventbus;

import com.edgar.util.event.Event;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/6/30.
 *
 * @author Edgar  Date 2017/6/30
 */
public class EventCodec implements MessageCodec<Event, Event> {
  @Override
  public void encodeToWire(Buffer buffer, Event event) {
    buffer.appendString(new JsonObject(event.toMap()).encode());
  }

  @Override
  public Event decodeFromWire(int pos, Buffer buffer) {
    System.out.println(buffer.toString());
    int length = buffer.getInt(pos);
    pos += 4;
    byte[] encoded = buffer.getBytes(pos, pos + length);
    JsonObject jsonObject = Buffer.buffer(encoded).toJsonObject();
    return Event.fromMap(jsonObject.getMap());
  }

  @Override
  public Event transform(Event event) {
    return Event.fromMap(event.toMap());
  }

  @Override
  public String name() {
    return EventCodec.class.getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
