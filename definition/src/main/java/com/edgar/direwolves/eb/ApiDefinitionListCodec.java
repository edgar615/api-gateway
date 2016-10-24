package com.edgar.direwolves.eb;

import com.edgar.direwolves.definition.ApiDefinition;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/10/8.
 *
 * @author Edgar  Date 2016/10/8
 */
public class ApiDefinitionListCodec implements MessageCodec<List<ApiDefinition>,
        List<ApiDefinition>> {

  @Override
  public void encodeToWire(Buffer buffer, List<ApiDefinition> apiDefinitions) {
    JsonArray jsonArray = new JsonArray();
    apiDefinitions.forEach(definition -> jsonArray.add(definition.toJson()));
    jsonArray.writeToBuffer(buffer);
  }

  @Override
  public List<ApiDefinition> decodeFromWire(int pos, Buffer buffer) {
    JsonArray jsonArray = buffer.toJsonArray();
    List<ApiDefinition> apiDefinitions = new ArrayList<>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++) {
      apiDefinitions.add(ApiDefinition.fromJson(jsonArray.getJsonObject(i)));
    }
    return apiDefinitions;
  }

  @Override
  public List<ApiDefinition> transform(List<ApiDefinition> apiDefinitions) {
    List<ApiDefinition> copy = new ArrayList<>(apiDefinitions.size());
    apiDefinitions.forEach(definition -> copy.add(definition.copy()));
    return copy;
  }

  @Override
  public String name() {
    return ApiDefinitionListCodec.class.getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
