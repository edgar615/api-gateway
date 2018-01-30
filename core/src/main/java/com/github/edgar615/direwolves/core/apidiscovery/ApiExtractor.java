package com.github.edgar615.direwolves.core.apidiscovery;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;

import java.util.List;

/**
 * Created by Edgar on 2018/1/30.
 *
 * @author Edgar  Date 2018/1/30
 */
public interface ApiExtractor {

  ApiDefinition extractor(List<ApiDefinition> definitions);
}
