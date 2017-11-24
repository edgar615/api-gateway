package com.github.edgar615.direwolves.standalone;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.edgar615.util.base.EncryptUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Edgar on 2016/8/25.
 *
 * @author Edgar  Date 2016/8/25
 */
public class Api {
    private String path;

    private Map<String, Object> params;

    private Map<String, Object> data;

    private String appKey = "RmOI7jCvDtfZ1RcAkea1";

    private String appSecret = "dbb0f95c8ebf4317942d9f5057d0b38e";

    private String signMethod = EncryptUtils.HMACMD5;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getSignMethod() {
        return signMethod;
    }

    public void setSignMethod(String signMethod) {
        this.signMethod = signMethod;
    }

    public String signTopRequest() {
        if (params == null) {
            params = new HashMap<>();
        }
        params.putIfAbsent("appKey", appKey);
        params.putIfAbsent("timestamp", Instant.now().getEpochSecond());
        params.putIfAbsent("v", "1.0");
        params.putIfAbsent("signMethod", signMethod);
        params.putIfAbsent("nonce", UUID.randomUUID().toString());

        if (data != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String body = objectMapper.writeValueAsString(data);
                params.put("body", body);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        // 第一步：检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 第二步：把所有参数名和参数值串在一起
        List<String> query = new ArrayList<>(params.size());
        List<String> base = new ArrayList<>(params.size());
        for (String key : keys) {
            String value = params.get(key).toString();
            if (!Strings.isNullOrEmpty(value)) {
                query.add(key + "=" + value);
            }
            if (!"body".equals(key)) {
                base.add(key + "=" + value);
            }
        }
        String queryString = Joiner.on("&").join(query);
        String sign = null;
        try {
            if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptHmacMd5(queryString, appSecret);
            } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptHmacSha256(queryString, appSecret);
            } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptHmacSha512(queryString, appSecret);
            } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
                sign = EncryptUtils.encryptMD5(appSecret + queryString + appSecret);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        System.out.println(queryString);
        System.out.println(Joiner.on("&").join(base) + "&sign=" + sign);
        return Joiner.on("&").join(base) + "&sign=" + sign;
    }
}