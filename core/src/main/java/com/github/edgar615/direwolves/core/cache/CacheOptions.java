package com.github.edgar615.direwolves.core.cache;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
public class CacheOptions {


  private static final long DEFAULT_EXPIRE_AFTER_WRITE = 60 * 30;

  /**
   * 缓存的过期时间，单位秒
   */
  private Long expireAfterWrite = DEFAULT_EXPIRE_AFTER_WRITE;

  public CacheOptions( ) {
  }

  public CacheOptions(JsonObject json) {
    this();
    CacheOptionsConverter.fromJson(json, this);
  }

  public Long getExpireAfterWrite() {
    return expireAfterWrite;
  }

  public CacheOptions setExpireAfterWrite(long expireAfterWrite) {
    this.expireAfterWrite = expireAfterWrite;
    return this;
  }
}
