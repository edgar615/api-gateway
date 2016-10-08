package com.edgar.direwolves.definition;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ApiDefinitionCodec implements MessageCodec<ApiDefinition, ApiDefinition> {
  @Override
  public void encodeToWire(Buffer buffer, ApiDefinition apiDefinition) {
    apiDefinition.toJson().writeToBuffer(buffer);
  }

  @Override
  public ApiDefinition decodeFromWire(int pos, Buffer buffer) {
    return ApiDefinition.fromJson(buffer.toJsonObject());
  }

  @Override
  public ApiDefinition transform(ApiDefinition apiDefinition) {
    JsonObject jsonObject = apiDefinition.toJson();
    return ApiDefinition.fromJson(jsonObject);
  }

  @Override
  public String name() {
    return "ApiDefinitionCodec";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
