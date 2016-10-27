package com.edgar.direwolves.definition;

import com.edgar.direwolves.core.spi.ApiDefinition;
import com.edgar.direwolves.core.spi.ApiDefinitionFactory;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by Edgar on 2016/10/27.
 *
 * @author Edgar  Date 2016/10/27
 */
public class ApiDefinitionFactoryImpl implements ApiDefinitionFactory {

  ApiDefinition create(String name, HttpMethod method, String path, String scope,
                       List<Endpoint> endpoints) {
    return new ApiDefinitionImpl(name, method, path, scope, endpoints);
  }

  @Override
  public ApiDefinition decode(JsonObject jsonObject) {
    return ApiDefinitionDecoder.instance().apply(jsonObject);
  }

  @Override
  public JsonObject encode(ApiDefinition definition) {
    return ApiDefinitionEncoder.instance().apply(definition);
  }
}
