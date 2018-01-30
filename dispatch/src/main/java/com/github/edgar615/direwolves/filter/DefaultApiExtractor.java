package com.github.edgar615.direwolves.filter;

import com.github.edgar615.direwolves.core.apidiscovery.ApiExtractor;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2018/1/30.
 *
 * @author Edgar  Date 2018/1/30
 */
public class DefaultApiExtractor implements ApiExtractor {
  @Override
  public ApiDefinition extractor(List<ApiDefinition> definitions) {
    return null;
  }

  private ApiDefinition matchApi(List<ApiDefinition> apiDefinitions) {
    if (apiDefinitions.isEmpty()) {//没有API
      throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND);
    }
    if (apiDefinitions.size() == 1) {//只有一个
      return apiDefinitions.get(0);
    }
    //优先选择正则匹配的API
    List<ApiDefinition> regexApiList = apiDefinitions.stream()
            .filter(d -> !d.antStyle())
            .collect(Collectors.toList());
    if (regexApiList.isEmpty()) {
      List<ApiDefinition> antApiList = apiDefinitions.stream()
              .filter(d -> d.antStyle())
              .collect(Collectors.toList());
      return extractApi(antApiList);
    } else {
      return extractApi(regexApiList);
    }
  }

  private ApiDefinition extractApi(List<ApiDefinition> apiDefinitions) {
    if (apiDefinitions.isEmpty()) {//没有API
      throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND);
    }
    if (apiDefinitions.size() != 1) {//有多个异常
      throw SystemException.create(DefaultErrorCode.CONFLICT);
    }
    return apiDefinitions.get(0);
  }
}
