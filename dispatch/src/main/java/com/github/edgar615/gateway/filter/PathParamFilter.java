package com.github.edgar615.gateway.filter;

import com.github.edgar615.gateway.core.definition.RegexPathApiDefinition;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.util.log.Log;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该filter将API定义中的正则表达式与请求路径做匹配，将API定义中的正则表达式所对应的值转换为对应的参数.
 * 参数名为param0  0表示第几个正则表达式，从0开始计算
 * 参数值为正则表达式在请求路径中的值
 *
 * 所有的参数名将保存在上下文变量中，可以通过$var.param0变量来获得。
 * <p>
 * 示例:API定义的路径为/devices/([\d+]+)，请求的路径为/devices/1，那么对应的参数名为param0，参数值为1
 * <p>
 * Created by edgar on 17-1-4.
 */
public class PathParamFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathParamFilter.class);

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 2000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition() != null
            && apiContext.apiDefinition().regexStyle();
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    RegexPathApiDefinition apiDefinition = (RegexPathApiDefinition) apiContext.apiDefinition();
    Pattern pattern = apiDefinition.pattern();
    String path = apiContext.path();
    Matcher matcher = pattern.matcher(path);
    if (matcher.matches()) {
      try {
        for (int i = 0; i < matcher.groupCount(); i++) {
          String group = matcher.group(i + 1);
          if (group != null) {
            final String k = "param" + i;
            final String value = URLDecoder.decode(group, "UTF-8");
            apiContext.addVariable(k, value);
          }
        }
      } catch (UnsupportedEncodingException e) {
        Log.create(LOGGER)
                .setLogType("Filter")
                .setEvent("PathParam")
                .setTraceId(apiContext.id())
                .setEvent("path.decode.failed")
                .warn();
      }
    }
    completeFuture.complete(apiContext);
  }

}
