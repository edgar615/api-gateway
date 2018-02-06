package com.github.edgar615.direwolves.plugin.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Consts;
import com.github.edgar615.direwolves.core.utils.MultimapUtils;
import com.github.edgar615.util.base.EncryptUtils;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 *   appkey : {
 *   data : APPKEY的JSON数组，默认为[]
 *   url: http获取appkey的接口地址，这个地址对应了一个API路由（不应该做appkey校验，而且只能内外访问）,如果没有这个配置，则不会从后端查询appkey
 *   cache: {
 *      "expireAfterWrite": 1800, 缓存的过期时间，单位秒，默认值1800
 *      "maximumSize": 5000，缓存的最大数量，默认值5000
 *      },
 *   }
 * </pre>
 * <p>
 * 一个正确的appKey的JSON格式应该包括 appKey, appSecret, appCode, permissions四个属性。
 * 如果通过HTTP获取appKey的下游服务返回的JSON格式不是这几个属性，可以通过ResponseTransformerPlugin进行转换
 * <p>
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
public class AppKeyFilter implements Filter {

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private final Vertx vertx;

  private final AppKeyFinder appKeyFinder;

  AppKeyFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    commonParamRule.put("appKey", Rule.required());
    commonParamRule.put("nonce", Rule.required());
    commonParamRule.put("signMethod", Rule.required());
    List<Object> optionalRule = new ArrayList<>();
    optionalRule.add("HMACSHA256");
    optionalRule.add("HMACSHA512");
    optionalRule.add("HMACMD5");
    optionalRule.add("MD5");
    commonParamRule.put("signMethod", Rule.optional(optionalRule));
    commonParamRule.put("sign", Rule.required());

    int port = config.getInteger("port", Consts.DEFAULT_PORT);
    JsonObject appKeyConfig = config.getJsonObject("appkey", new JsonObject());
    appKeyConfig.put("port", port);
    this.appKeyFinder = new AppKeyFinder(vertx, appKeyConfig);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 8000;
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
    appKeyFinder.load(appKey, ar -> {
      if (ar.failed()) {
        SystemException e = SystemException.create(DefaultErrorCode.INVALID_REQ)
                .set("details", "Non-existent AppKey:" + appKey);
        failed(completeFuture, apiContext.id(), "AppKeyNonExistent", e);
        return;
      }
      JsonObject jsonObject = ar.result();
      checkSign(apiContext, completeFuture, params, clientSignValue, signMethod, jsonObject);
    });

  }

  private void checkSign(ApiContext apiContext, Future<ApiContext> completeFuture,
                         Multimap<String, String> params, String clientSignValue, String signMethod,
                         JsonObject app) {
    String secret = app.getString("appSecret", "UNKOWNSECRET");
    String serverSignValue = signTopRequest(params, secret, signMethod);
    if (!clientSignValue.equalsIgnoreCase(serverSignValue)) {
      SystemException e = SystemException.create(DefaultErrorCode.INVALID_REQ)
              .set("details", "Incorrect sign");
      failed(completeFuture, apiContext.id(), "appKey.tripped", e);
    } else {
      apiContext.addVariable("client.appKey", app.getString("appKey", "anonymous"));
      if (app.containsKey("appId")) {
        apiContext.addVariable("client.appId", app.getValue("appId"));
      }
      if (app.containsKey("permissions")) {
        apiContext.addVariable("client.permissions", app.getValue("permissions"));
      }
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