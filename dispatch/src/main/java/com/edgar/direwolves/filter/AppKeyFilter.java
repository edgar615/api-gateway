package com.edgar.direwolves.filter;

import com.edgar.direwolves.definition.AuthDefinition;
import com.edgar.direwolves.definition.AuthDefinitionRegistry;
import com.edgar.direwolves.definition.AuthType;
import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.base.EncryptUtils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by edgar on 16-9-20.
 */
public class AppKeyFilter implements Filter {

    private static final String TYPE = "app_key";
    private final Multimap<String, Rule> commonParamRule = ArrayListMultimap.create();
    private int timeout = 5 * 60;
    private JsonArray secrets = new JsonArray();

    public AppKeyFilter() {
        commonParamRule.put("appKey", Rule.required());
        commonParamRule.put("timestamp", Rule.required());
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
        if (config.containsKey("app_key.expires")) {
            this.timeout = config.getInteger("app_key.expires");
        }
        if (config.containsKey("app_key.secret")) {
            JsonArray secretArray = config.getJsonArray("app_key.secret");
            secrets.addAll(secretArray);
        }
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        List<AuthDefinition> definitions = AuthDefinitionRegistry.create()
                .filter(apiContext.apiName(), AuthType.APP_KEY);
        return definitions.size() == 1;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        //校验参数
        Validations.validate(apiContext.params(), commonParamRule);
        Multimap<String, String> params = ArrayListMultimap.create(apiContext.params());
        //检查时间戳
        Integer timestamp = Integer.parseInt(getFirst(params, "timestamp").toString());
        long currentTime = Instant.now().getEpochSecond();
        if ((timestamp > currentTime + timeout)
                || (timestamp < currentTime - timeout)) {
            throw SystemException.create(DefaultErrorCode.EXPIRE);
        }

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
            throw SystemException.create(DefaultErrorCode.INVALID_REQ);
        }
        String secret = company.getString("appSecret", "UNKOWNSECRET");

        String serverSignValue = signTopRequest(params, secret, signMethod);
        if (!clientSignValue.equals(serverSignValue)) {
            throw SystemException.create(DefaultErrorCode.INVALID_REQ);
        }
        apiContext.addVariable("appCode", company.getInteger("code", 0));
        //检查有没有访问权限
//        String scope = company.getString("scope", "");
//        List<String> scopeList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(scope);
//        scopeList = new ArrayList<>(scopeList);
//        scopeList.add("default");
//        String apiScope = apiContext.
//        if (!scopeList.contains("all") && !scopeList.contains(apiScope)) {
//            throw SystemException.create(DefaultErrorCode.NO_AUTHORITY);
//        }
//        rc.put("companyCode", company.getInteger("companyCode", -1));
//        rc.put("scope", company.getString("scope", "default"));
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

    private String getFirst(Multimap<String, String> params, String paramName) {
        return Lists.newArrayList(params.get(paramName)).get(0);
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
