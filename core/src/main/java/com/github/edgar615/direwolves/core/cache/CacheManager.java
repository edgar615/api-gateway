package com.github.edgar615.direwolves.core.cache;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
public interface CacheManager {
  Cache getCache(String cacheName);

  Cache addCache(Cache cache);

  static CacheManager instance() {
    return CacheManagerImpl.instance();
  }
}
