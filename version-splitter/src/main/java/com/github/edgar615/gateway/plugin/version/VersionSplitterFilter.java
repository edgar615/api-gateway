package com.github.edgar615.gateway.plugin.version;

import com.google.common.base.Strings;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.util.base.VersionUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 该filter根据请求从API路由注册表中读取到对应的API定义.
 * 按照灰度发布规则匹配
 * <p>
 * 该filter可以接受下列的配置参数
 * <pre>
 * </pre>
 * <p>
 * <b>该filter应该在所有的filter之前执行</b>如果未找到对应的API定义，直接返回对应的异常。
 * 该filter的order=-2147484548, int的最小值
 * Created by edgar on 17-1-4.
 */
public class VersionSplitterFilter implements Filter {

  private final Vertx vertx;

  private final ApiDiscovery discovery;

  public VersionSplitterFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonObject dicoveryConfig = config.getJsonObject("api.discovery", new JsonObject());
    this.discovery = ApiDiscovery.create(vertx,
                                                 new ApiDiscoveryOptions(dicoveryConfig));
  }

  @Override
  public String type() {
    return Filter.PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 900;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition() == null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    discovery.filter(apiContext.method().name(), apiContext.path(), ar -> {
      if (ar.failed()) {
        failed(completeFuture, apiContext.id(), "ApiMatchFailure", ar.cause());
        return;
      }
      try {
        List<ApiDefinition> apiDefinitions = ar.result();
        ApiDefinition apiDefinition = matchApi(ApiDefinition.extractInOrder(apiDefinitions), apiContext);
        completeFuture.complete(apiContext.setApiDefinition(apiDefinition));
        return;
      } catch (SystemException e) {
        e.set("details", String.format("ApiMatchFailure %s:%s",
                                       apiContext.method().name(),
                                       apiContext.path()));
        failed(completeFuture, apiContext.id(), "ApiMatchFailure", e);
        return;
      } catch (Exception e) {
        failed(completeFuture, apiContext.id(), "ApiMatchFailure", e);
        return;
      }
    });
  }

  private ApiDefinition matchApi(List<ApiDefinition> apiDefinitions, ApiContext apiContext) {
    if (apiDefinitions.isEmpty()) {//没有API
      throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND);
    }
    if (apiDefinitions.size() == 1) {//只有一个，直接使用它
      return apiDefinitions.get(0);
    }
    List<ApiDefinition> grayDefinitions = apiDefinitions.stream()
            .filter(d -> d.plugin(VersionSplitterPlugin.class.getSimpleName()) != null)
            .collect(Collectors.toList());

    if (grayDefinitions.size() != 1) {//有且只能由一个匹配版本号的插件，其他情况异常
      throw SystemException.create(DefaultErrorCode.CONFLICT);
    }
    ApiDefinition grayDefinition = grayDefinitions.get(0);
    VersionSplitterPlugin plugin =
            (VersionSplitterPlugin) grayDefinition
                    .plugin(VersionSplitterPlugin.class.getSimpleName());
    return matchWithVersion(apiDefinitions, apiContext, plugin);
  }

  private ApiDefinition matchWithVersion(List<ApiDefinition> apiDefinitions, ApiContext apiContext,
                                         VersionSplitterPlugin plugin) {
    String version = plugin.traffic().decision(apiContext);
    if (Strings.isNullOrEmpty(version)) {
      //按照默认设置来匹配
      return matchDefault(apiDefinitions, plugin);
    }
    List<ApiDefinition> matchDefinitions = apiDefinitions.stream()
            .filter(d -> {
              VersionPlugin versionPlugin =
                      (VersionPlugin) d.plugin(VersionPlugin.class.getSimpleName());
              return versionPlugin != null
                     && versionPlugin.version().equalsIgnoreCase(version);
            })
            .collect(Collectors.toList());
    if (matchDefinitions.size() > 1) {//匹配到多个API，异常
      throw SystemException.create(DefaultErrorCode.CONFLICT);
    } else if (matchDefinitions.size() == 1) {//匹配
      return matchDefinitions.get(0);
    } else {
      return matchDefault(apiDefinitions, plugin);
    }

  }

  private ApiDefinition matchDefault(List<ApiDefinition> apiDefinitions,
                                     VersionSplitterPlugin plugin) {
    Comparator<ApiDefinition> comparator = (o1, o2) -> {
      VersionPlugin v1 = (VersionPlugin) o1.plugin(VersionPlugin.class.getSimpleName());
      VersionPlugin v2 = (VersionPlugin) o2.plugin(VersionPlugin.class.getSimpleName());
      String version1 = v1.version();
      String version2 = v2.version();
      if ((version1.startsWith("v") || version1.startsWith("V"))
          && (version2.startsWith("v") || version2.startsWith("V"))) {
        return VersionUtils.compareVersion(version1, version2);
      }
      return v1.version().compareTo(v2.version());
    };
    if ("ceil".equalsIgnoreCase(plugin.unSatisfyStrategy())) { //向上匹配
      return apiDefinitions.stream()
              .filter(d -> d.plugin(VersionPlugin.class.getSimpleName()) != null)
              .max(comparator).get();
    }
    //默认向下匹配 floor
    return apiDefinitions.stream()
            .filter(d -> d.plugin(VersionPlugin.class.getSimpleName()) != null)
            .min(comparator).get();
  }

}
