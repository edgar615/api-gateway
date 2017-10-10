package com.github.edgar615.direwolves.core.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Edgar on 2017/8/30.
 *
 * @author Edgar  Date 2017/8/30
 */
class CacheManagerImpl implements CacheManager {
  private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap();

  private static final CacheManager INSTANCE = new CacheManagerImpl();

  private CacheManagerImpl() {}

  static CacheManager instance() {
    return INSTANCE;
  }

  @Override
  public Cache getCache(String cacheName) {
    return cacheMap.get(cacheName);
  }

  @Override
  public Cache addCache(Cache cache) {
    return cacheMap.put(cache.name(), cache);
  }

}
