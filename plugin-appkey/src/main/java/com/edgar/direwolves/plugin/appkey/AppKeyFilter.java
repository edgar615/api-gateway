package com.edgar.direwolves.plugin.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Helper;
import com.edgar.direwolves.core.utils.Log;
import com.edgar.direwolves.core.utils.MultimapUtils;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppKey的校验.
 * 在校验通过之后，会在上下文中存入变量:
 * <pre>
 *   app.code : appKey对应的编码，默认值0
 *   app.permissions : appKey的权限范围
 * </pre>
 * * 该filter可以接受下列的配置参数
 * <pre>
 *   namespace 项目的命名空间，用来避免多个项目冲突，默认值""
 *   app.secretKey 密钥的键值，默认值appSecret
 *   app.codeKey 编码的键值，默认值appCode
 *   app.permissionKey 权限的键值，默认值permissions
 *   app.origin 初始值，数组
 *   app.importer AppKey的导入,JsonObject，两个属性：scan-period导入周期，默认5000毫秒，url地址，默认值/appkey/import
 *   ，注意需要在路由中配置/appkey/import路由，并限制只有127.0.0.1的IP才允许访问
 * </pre>
 * 该filter的order=10
 * <p>
 * 如果开启了这个过滤器，那么对API的调用必须包含下列参数，如果缺少任意一个，服务端会认为是非法请求。
 * <pre>
 *   appKey	应用key	string	是	服务端每个第三方应用分配的appKey
 *   nonce	随机数	string	是	主要保证签名不可预测
 *   v	API协议版本	string	是	当前固定为1.0
 *    signMethod	签名的摘要算法	string	是	可选值MD5、HMACSHA256、HMACSHA512、 HMACMD5
 *    sign	签名	string	是	根据签名算法生成的签名，详细内容参考签名章节
 * </pre>
 * <p>
 * 签名生成的通用步骤如下：
 * 第一步，设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1=value1&key2=value2
 * …）拼接成字符串stringA，如果请求带请求体，将请求体中的JSON对象转换为字符串之后按照body=JSON的格式加入到URL键值中，拼接成字符串stringA。
 * 第二步，对stringA按照signMethod中指定的算法进行加密得到最终的signValue。
 * 如果是MD5加密，需要在stringA的首尾加上appSecret。
 * 第三步，将sign=signValue追加到URL参数的后面，向服务端发送请求。
 * <pre>
 *   示例1,GET请求 查询安防记录的接口
 * GET /alarms?type=21&alarmTimeStart=1469280456&alarmTimeEnd=1471958856&start=0&limit=20
 * 第一步，增加通用参数
 * /alarms?type=21&alarmTimeStart=1469280456&alarmTimeEnd=1471958856&start=0&limit=20&appKey
 * =XXXXX&timestamp=1471958856&nonce=123456&v=1.0&signMethod=HMACMD5
 *
 * 第二步，将所有的参数排序得到新的查询字符串
 * alarmTimeEnd=1471958856&alarmTimeStart=1469280456&appKey=XXXXX&limit=20&nonce=123456
 * &signMethod=HMACMD5&start=0&timestamp=1471958856&type=21&v=1.0
 *
 * 第三步，将上一步得到的查询字符串使用HMACMD5加密，得到签名7B686C90ACE0193430774F4BE096F128，并追加到查询参数之后
 * alarmTimeEnd=1471958856&alarmTimeStart=1469280456&
 * appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&timestamp=1471958856&type=21&v=1
 * .0&sign= 7B686C90ACE0193430774F4BE096F128
 *
 * 第四步，将上一步得到的查询字符串加入到接口中调用/alarms? alarmTimeEnd=1471958856&alarmTimeStart=1469280456&
 * appKey=XXXXX&limit=20&nonce=123456&signMethod=HMACMD5&start=0&timestamp=1471958856&type=21&v=1
 * .0&sign= 7B686C90ACE0193430774F4BE096F128
 *
 * 示例2,POST请求 用户登录
 * POST /login
 * {"username":"foo","password":"bar"}
 * 第一步，增加通用参数
 * /login?appKey=XXXXX&timestamp=1471958856&nonce=123456&v=1.0&signMethod=HMACMD5
 *
 * 第二步，将请求体转换为JSON字符串后追加到参数列表中
 * appKey=XXXXX&timestamp=1471958856&nonce=123456&v=1
 * .0&signMethod=HMACMD5&body={"username":"foo","password":"bar"}
 *
 * 第二步，将所有的参数排序得到新的查询字符串
 * appKey=XXXXX&body={"username":"foo",
 * "password":"bar"}&nonce=123456&signMethod=HMACMD5&timestamp=1471958856&v=1.0
 *
 * 第三步，将上一步得到的查询字符串使用HMACMD5加密，得到签名A61C44F04361DE0530F4EF2E363C4A45，并追加到查询参数之后（不包括body）
 * appKey=XXXXX&nonce=123456&signMethod=HMACMD5&timestamp=1471958856&v=1.0&sign=
 * A61C44F04361DE0530F4EF2E363C4A45
 *
 * 第四步，将上一步得到的查询字符串加入到接口中调用
 * /login?appKey=XXXXX&nonce=123456&signMethod=HMACMD5&timestamp=1471958856&v=1.0&sign=
 * A61C44F04361DE0530F4EF2E363C4A45
 *
 * </pre>
 * <p>
 * Created by edgar on 16-9-20.
 */
public class AppKeyFilter implements Filter, AppKeyPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppKeyFilter.class);

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final String namespace;

  private final RedisProvider redisProvider;

  private final String secretKey;

  private final String codeKey;

  private final String permissionsKey;

  private final Map<String, JsonObject> origins = new HashMap<>();

  private final Vertx vertx;

  AppKeyFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    commonParamRule.put("appKey", Rule.required());
    commonParamRule.put("nonce", Rule.required());
    commonParamRule.put("v", Rule.required());
    commonParamRule.put("signMethod", Rule.required());
    List<Object> optionalRule = new ArrayList<>();
    optionalRule.add("HMACSHA256");
    optionalRule.add("HMACSHA512");
    optionalRule.add("HMACMD5");
    optionalRule.add("MD5");
    commonParamRule.put("signMethod", Rule.optional(optionalRule));
    commonParamRule.put("sign", Rule.required());
    this.namespace = config.getString("namespace", "");
    String address = RedisProvider.class.getName();
    if (!Strings.isNullOrEmpty(namespace)) {
      address = namespace + "." + address;
    }
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
    this.secretKey = config.getString("app.secretKey", "appSecret");
    this.codeKey = config.getString("app.codeKey", "appCode");
    this.permissionsKey = config.getString("app.permissionKey", "permissions");

    JsonArray appKeys = config.getJsonArray("app.origin", new JsonArray());
    for (int i = 0; i < appKeys.size(); i++) {
      String appKey = appKeys.getJsonObject(i).getString("appKey");
      if (appKey != null) {
        origins.put(appKey, appKeys.getJsonObject(i));
      }
    }
    if (config.containsKey("app.importer")) {
      JsonObject importedConfig = config.getJsonObject("app.importer");
      importedConfig.put("http.port", config.getInteger("http.port", 9000));
      AppKeyImporter importer = new AppKeyImporter(vertx, this, importedConfig);
      importer.start();
    }
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(AppKeyPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    //校验参数
    Validations.validate(apiContext.params(), commonParamRule);
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    String clientSignValue = MultimapUtils.getFirst(params, "sign").toString();
    String signMethod = MultimapUtils.getFirst(params, "signMethod").toString();
    String appKey = MultimapUtils.getFirst(params, "appKey").toString();
    params.removeAll("sign");
    if (apiContext.body() != null) {
      params.removeAll("body");
      params.put("body", apiContext.body().encode());
    }
    if (origins.containsKey(appKey)) {
      JsonObject app = origins.get(appKey);
      checkSign(apiContext, completeFuture, params, clientSignValue, signMethod, app);
    } else {
      String appCacheKey = namespace + ":appKey:" + appKey;
      redisProvider.get(appCacheKey, ar -> {
        if (ar.succeeded()) {
          JsonObject app = ar.result();
          checkSign(apiContext, completeFuture, params, clientSignValue, signMethod, app);
        } else {
          Log.create(LOGGER)
                  .setTraceId(apiContext.id())
                  .setEvent("appkey.undefined")
                  .addData("appkey", appKey)
                  .error();
          completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_REQ)
                                      .set("details", "Undefined AppKey:" + appKey));
        }
      });
    }
  }

  @Override
  public void publish(JsonObject jsonObject, Handler<AsyncResult<Void>> resultHandler) {
    if (!jsonObject.containsKey("appKey")) {
      resultHandler.handle(Future.failedFuture("undefined appKey"));
      return;
    }
    if (!jsonObject.containsKey(secretKey)) {
      resultHandler.handle(Future.failedFuture("undefined " + secretKey));
      return;
    }
    if (!jsonObject.containsKey(codeKey)) {
      resultHandler.handle(Future.failedFuture("undefined " + codeKey));
      return;
    }
    if (!jsonObject.containsKey(permissionsKey)) {
      resultHandler.handle(Future.failedFuture("undefined " + permissionsKey));
      return;
    }
    origins.put(jsonObject.getString("appKey"), jsonObject);
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void unpublish(String appKey, Handler<AsyncResult<Void>> resultHandler) {
    origins.remove(appKey);
    resultHandler.handle(Future.succeededFuture());
  }

  private void checkSign(ApiContext apiContext, Future<ApiContext> completeFuture,
                         Multimap<String, String> params, String clientSignValue, String signMethod,
                         JsonObject app) {
    String secret = app.getString(secretKey, "UNKOWNSECRET");
    String serverSignValue = signTopRequest(params, secret, signMethod);
    if (!clientSignValue.equals(serverSignValue)) {
      Log.create(LOGGER)
              .setTraceId(apiContext.id())
              .setEvent("sign.tripped")
              .addData("baseString", baseString(params))
              .error();
      completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_REQ)
                                  .set("details", "The sign is incorrect"));
    } else {
//      Multimap<String, String> newParams = ArrayListMultimap.create(apiContext.params());
////      newParams.removeAll("sign");
////      newParams.removeAll("signMethod");
////      newParams.removeAll("v");
////      newParams.removeAll("appKey");
//      ApiContext newContext =
//              ApiContext.create(apiContext.id(), apiContext.method(), apiContext.path(),
//                                apiContext.headers(), newParams, apiContext.body
//                              ());
//      ApiContext.copyProperites(apiContext, newContext);
      apiContext.addVariable("app.appKey", app.getString("appKey", "anonymous"));
      apiContext.addVariable("app.code", app.getInteger(codeKey, 0));
      apiContext.addVariable("app.permissions", app.getString(permissionsKey, "default"));
      completeFuture.complete(apiContext);
    }
  }

  private String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
    String queryString = baseString(params);

    String sign = null;
    try {
      if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacMd5(queryString, secret);
      } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha256(queryString, secret);
      } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptHmacSha512(queryString, secret);
      } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
        sign = EncryptUtils.encryptMD5(secret + queryString + secret);
      }
    } catch (IOException e) {

    }
    return sign;
  }

  private String baseString(Multimap<String, String> params) {// 第一步：检查参数是否已经排序
    String[] keys = params.keySet().toArray(new String[0]);
    Arrays.sort(keys);

    // 第二步：把所有参数名和参数值串在一起
    List<String> query = new ArrayList<>(params.size());
    for (String key : keys) {
      if (!key.startsWith("$param")) {
        String value = MultimapUtils.getFirst(params, key);
        if (!Strings.isNullOrEmpty(value)) {
          query.add(key + "=" + value);
        }
      }
    }
    return Joiner.on("&").join(query);
  }
}