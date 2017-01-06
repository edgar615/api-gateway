package com.edgar.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by edgar on 17-1-4.
 */
public class PathParamFilter implements Filter {
  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 10;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition() != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    Pattern pattern = apiContext.apiDefinition().pattern();
    String path = apiContext.path();
    Matcher matcher = pattern.matcher(path);
    if (matcher.matches()) {
      try {
        for (int i = 0; i < matcher.groupCount(); i++) {
          String group = matcher.group(i + 1);
          if (group != null) {
            final String k = "param" + i;
            final String value = URLDecoder.decode(group, "UTF-8");
            params.put(k, value);
          }
        }
      } catch (UnsupportedEncodingException e) {
        //TODO 异常处理
      }
    }
    ApiContext newApiContext =
            ApiContext.create(apiContext.id(), apiContext.method(), apiContext.path(),
                              apiContext.headers(), params, apiContext.body());
    ApiContext.copyProperites(apiContext, newApiContext);
    completeFuture.complete(newApiContext);
  }

}
