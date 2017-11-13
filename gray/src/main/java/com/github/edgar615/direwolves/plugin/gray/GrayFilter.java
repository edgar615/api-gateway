package com.github.edgar615.direwolves.plugin.gray;

import com.google.common.base.Strings;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.direwolves.core.apidiscovery.ApiFinder;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.MultimapUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.log.Log;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class GrayFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrayFilter.class);

  private static final String HEADER_NAME = "x-api-version";

  private final Vertx vertx;

  private final ApiFinder apiFinder;

  public GrayFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String namespace = config.getString("namespace", "api-gateway");
    ApiDiscovery discovery = ApiDiscovery.create(vertx,
                                                 new ApiDiscoveryOptions()
                                                         .setName(namespace));
    this.apiFinder = ApiFinder.create(vertx, discovery);
  }

  @Override
  public String type() {
    return PRE;
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
    apiFinder.getDefinitions(apiContext.method().name(), apiContext.path(), ar -> {
      if (ar.failed()) {
        Log.create(LOGGER)
                .setTraceId(apiContext.id())
                .setEvent("api.discovery.failed")
                .error();
        completeFuture.fail(ar.cause());
        return;
      }
      try {
        List<ApiDefinition> apiDefinitions = ar.result();
        String reqVersion = MultimapUtils.getCaseInsensitive(apiContext.headers(), HEADER_NAME);
        ApiDefinition apiDefinition = matchApi(apiDefinitions, reqVersion);
        completeFuture.complete(apiContext.setApiDefinition(apiDefinition));
        return;
      } catch (SystemException e) {
        e.set("details", "Undefined Api")
                .set("api", apiContext.method().name() + " " + apiContext.path());
        failed(apiContext, completeFuture, e);
        return;
      } catch (Exception e) {
        failed(apiContext, completeFuture, e);
        return;
      }
    });
  }

  private void failed(ApiContext apiContext, Future<ApiContext> completeFuture, Exception e) {
    Log.create(LOGGER)
            .setTraceId(apiContext.id())
            .setEvent("api.discovery.failed")
            .error();
    completeFuture.fail(e);
  }


  private ApiDefinition matchApi(List<ApiDefinition> apiDefinitions, String reqVersion) {
    if (apiDefinitions.isEmpty()) {//没有API
      throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND);
    }
    if (apiDefinitions.size() == 1) {//只有一个，直接使用它
      return apiDefinitions.get(0);
    }
    List<ApiDefinition> grayDefinitions = apiDefinitions.stream()
            .filter(d -> d.plugin(HeaderGrayPlugin.class.getSimpleName()) != null)
            .collect(Collectors.toList());
    if (grayDefinitions.size() != 1) {//有且只能由一个灰度插件，其他情况异常
      throw SystemException.create(DefaultErrorCode.CONFLICT);
    }
    ApiDefinition grayDefinition = grayDefinitions.get(0);
    HeaderGrayPlugin headerGrayPlugin =
            (HeaderGrayPlugin) grayDefinition.plugin(HeaderGrayPlugin.class.getSimpleName());

    if (Strings.isNullOrEmpty(reqVersion)) {
      //没有请求头，使用默认配置
      return matchDefault(apiDefinitions, headerGrayPlugin);
    } else {
      return matchWithVersion(apiDefinitions, reqVersion, headerGrayPlugin);
    }
  }

  private ApiDefinition matchWithVersion(List<ApiDefinition> apiDefinitions, String reqVersion,
                                         HeaderGrayPlugin grayPlugin) {
    List<ApiDefinition> matchDefinitions = apiDefinitions.stream()
            .filter(d -> {
              VersionPlugin versionPlugin =
                      (VersionPlugin) d.plugin(VersionPlugin.class.getSimpleName());
              return versionPlugin != null
                     && versionPlugin.version().equalsIgnoreCase(reqVersion);
            })
            .collect(Collectors.toList());
    if (matchDefinitions.size() > 1) {//匹配到多个API，异常
      throw SystemException.create(DefaultErrorCode.CONFLICT);
    }
    if (matchDefinitions.size() == 1) {//匹配
      return matchDefinitions.get(0);
    }
    //没有匹配，使用默认
    return matchDefault(apiDefinitions, grayPlugin);
  }

  private ApiDefinition matchDefault(List<ApiDefinition> apiDefinitions,
                                     HeaderGrayPlugin grayPlugin) {
    Comparator<ApiDefinition> comparator = (o1, o2) -> {
      VersionPlugin v1 = (VersionPlugin) o1.plugin(VersionPlugin.class.getSimpleName());
      VersionPlugin v2 = (VersionPlugin) o2.plugin(VersionPlugin.class.getSimpleName());
      return v1.version().compareTo(v2.version());
    };
    if ("ceil".equalsIgnoreCase(grayPlugin.type())) { //向上匹配
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
