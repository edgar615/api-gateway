package com.edgar.direwolves.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AppKey的校验.
 * 在校验通过之后，会在上下文中存入变量:
 * <pre>
 *   app_key.code : app编码，默认值0
 *   app_key.scope : app接口范围,默认值default
 * </pre>
 * <p>
 * 如果开启了这个过滤器，那么对API的调用必须包含下列参数，如果缺少任意一个，服务端会认为是非法请求。
 * <pre>
 *   appKey	应用key	string	是	服务端每个第三方应用分配的appKey
 *   nonce	随机数	string	是	主要保证签名不可预测
 *   v	API协议版本	string	是	当前固定为1.0
 *    signMethod	签名的摘要算法	string	是	可选值MD5、HMACSHA256、HMACSHA512、 HMACMD5
 *    sign	签名	string	是	根据签名算法生成的签名，详细内容参考签名章节
 * </pre>
 * 默认没有任何appKey，可以通过app_key.secret的配置项来指定appKey，该配置项接收[ { "key" : "1", "secret" : "2", "code" :
 * 0, "scope" : "all" } ]格式的JSON数组
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

  private static final String TYPE = "app_key";

  private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();

  private Vertx vertx;

  private JsonArray secrets = new JsonArray();

  public AppKeyFilter() {
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
  }

  @Override
  public String type() {
    return TYPE;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    JsonArray secretArray = config.getJsonArray("app_key.secret", new JsonArray());
    secrets.addAll(secretArray);
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    if (apiContext.apiDefinition() == null) {
      return false;
    }
    List<String> filters = apiContext.apiDefinition().filters();
    return filters.contains(TYPE);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    //校验参数
    Validations.validate(apiContext.params(), commonParamRule);
    Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
    String clientSignValue = getFirst(params, "sign").toString();
    String signMethod = getFirst(params, "signMethod").toString();
    String appKey = getFirst(params, "appKey").toString();
    params.removeAll("sign");
    if (apiContext.body() != null) {
      params.removeAll("body");
      params.put("body", apiContext.body().encode());
    }

    JsonObject company = filterByAppKey(appKey);
    if (company == null) {
      completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_REQ));
    } else {
      String secret = company.getString("secret", "UNKOWNSECRET");

      String serverSignValue = signTopRequest(params, secret, signMethod);
      if (!clientSignValue.equals(serverSignValue)) {
        completeFuture.fail(SystemException.create(DefaultErrorCode.INVALID_REQ));
      } else {
        apiContext.addVariable("app_key.code", company.getInteger("code", 0));
        apiContext.addVariable("app_key.scope", company.getString("scope", "default"));

        apiContext.params().removeAll("sign");
        apiContext.params().removeAll("signMethod");
        apiContext.params().removeAll("v");
        apiContext.params().removeAll("appKey");
        completeFuture.complete(apiContext);
      }
    }
  }

  private JsonObject filterByAppKey(String appKey) {

    JsonObject appJson = null;
    for (int i = 0; i < secrets.size(); i++) {
      JsonObject c = secrets.getJsonObject(i);
      String key = c.getString("key");
      if (appKey.equalsIgnoreCase(key)) {
        appJson = c;
      }
    }
    return appJson;
  }

  private String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
    // 第一步：检查参数是否已经排序
    String[] keys = params.keySet().toArray(new String[0]);
    Arrays.sort(keys);

    // 第二步：把所有参数名和参数值串在一起
    List<String> query = new ArrayList<>(params.size());
    for (String key : keys) {
      String value = getFirst(params, key);
      if (!Strings.isNullOrEmpty(value)) {
        query.add(key + "=" + value);
      }
    }
    String queryString = Joiner.on("&").join(query);
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
}
