package com.github.edgar615.direvolves.plugin.auth;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据userId从下游服务中拉取用户信息，并且更新到principal
 * * 该filter可以接受下列的配置参数
 * <pre>
 *   namespace 项目的命名空间，用来避免多个项目冲突，默认值""
 * </pre>
 * user
 * <pre>
 * "user" : {
 * "loader" : "/users", //对应的API地址，最终发送的请求为http://127.0.0.1:${port}/{loader}?userId=${userId}
 * "cache": {
 * "type" : "local", //缓存类型 redis或local
 * "expireAfterWrite": 3600, // 过期时间
 * "maximumSize": 5000 //最大值
 * }
 * }
 * </pre>
 * 该filter的order=11000
 * Created by edgar on 16-11-26.
 */
public class UserLoaderFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserLoaderFilter.class);

  private final Vertx vertx;

  private final String userKey = "userId";

  private final UserFinder userFinder;

  /**
   * @param vertx  Vertx
   * @param config 配置
   */
  UserLoaderFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonObject userConfig = config.getJsonObject("user", new JsonObject());
    int port = config.getInteger("port", Consts.DEFAULT_PORT);
    userConfig.put("port", port);
    this.userFinder = new UserFinder(vertx, userConfig);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 11000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.principal() == null) {
      return false;
    }
    return apiContext.apiDefinition()
                   .plugin(UserLoaderPlugin.class.getSimpleName()) != null
           && apiContext.principal().containsKey(userKey);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    Object userId = apiContext.principal().getValue(userKey);
    userFinder.find(userId.toString(), ar -> {
      if (ar.failed()) {
        SystemException e = SystemException.create(DefaultErrorCode.UNKOWN_ACCOUNT)
                .set("details", "Non-existent User:" + userId);
        failed(completeFuture, apiContext.id(), "UserNonExistent", e);
        return;
      }
      apiContext.principal().mergeIn(ar.result());
      completeFuture.complete(apiContext);
    });
  }

}
