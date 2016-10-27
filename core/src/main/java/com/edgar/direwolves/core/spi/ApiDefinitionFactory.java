package com.edgar.direwolves.core.spi;

import io.vertx.core.http.HttpMethod;

import java.util.List;

/**
 * Created by Edgar on 2016/10/27.
 *
 * @author Edgar  Date 2016/10/27
 */
public interface ApiDefinitionFactory extends JsonObjectCodec<ApiDefinition> {
  ApiDefinition create(String name, HttpMethod method, String path, String scope,
                       List<Endpoint> endpoints);
}
