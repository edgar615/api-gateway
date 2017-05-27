package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.JsonUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 检查用户的appCode和appkey的appCode是否一致.
 * <pre>
 *   app.codeKey 编码的键值，默认值appCode
 * </pre>
 * 该filter的order=1000
 *
 * @author Edgar  Date 2016/10/31
 */
public class AppCodeVertifyFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppCodeVertifyFilter.class);


  private final String appCodeKey;

  private final Vertx vertx;

  AppCodeVertifyFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.appCodeKey = config.getString("app.codeKey", "appCode");
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 1010;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition()
                   .plugin(AppCodeVertifyPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    JsonObject user = apiContext.principal();
    if (user == null) {
      throw SystemException.create(DefaultErrorCode.UNKOWN_LOGIN);
    }
    int userCode = getInteger(user, appCodeKey, 0);
    int appCode = (int) apiContext.variables().getOrDefault("app.code", 0);
    if (userCode != appCode) {
      throw SystemException.create(DefaultErrorCode.INVALID_REQ)
              .set("details", "The user does not match the appkey");
    }
    completeFuture.complete(apiContext);
  }

  /**
   * 从一个JSON对象中获取int
   *
   * @param jsonObject JSON对象
   * @param key        键值
   * @return int类型的值，如果value不能转换为int，抛出异常
   */
  private Integer getInteger(JsonObject jsonObject, String key) {
    Object value = jsonObject.getValue(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return (Integer) value;
    }
    if (value instanceof String) {
      if (value.toString().equalsIgnoreCase("")) {
        return 0;
      }
      return Integer.parseInt(value.toString());
    }
    throw new ClassCastException(value.getClass() + " cannot be cast to java.lang.Integer");
  }

  /**
   * 从一个JSON对象中获取int
   *
   * @param jsonObject JSON对象
   * @param key        键值
   * @param def        默认值
   * @return int类型的值，如果value不能转换为int，抛出异常，如果没有该值，返回默认值
   */
  private Integer getInteger(JsonObject jsonObject, String key, int def) {
    Integer value = getInteger(jsonObject, key);

    if (value == null) {
      return def;
    }
    return value;
  }

}
