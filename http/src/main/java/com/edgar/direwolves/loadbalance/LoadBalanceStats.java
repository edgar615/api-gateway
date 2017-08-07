package com.edgar.direwolves.loadbalance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 服务节点的状态.
 * <p>
 * 这个类是一个单例类，内部使用LoadingCache实现了服务节点状态的缓存（1小时有效期）.
 *
 * @author Edgar  Date 2017/7/31
 */
public class LoadBalanceStats {

  private static final LoadBalanceStats INSTANCE = new LoadBalanceStats();

  private final LoadingCache<String, ServiceStats> cache;

  private LoadBalanceStats() {
    this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .removalListener((RemovalListener<String, ServiceStats>) notification -> {
//                notification.getValue().close();
            })
            .build(new CacheLoader<String, ServiceStats>() {
              @Override
              public ServiceStats load(String serviceId) throws Exception {
                return new ServiceStats(serviceId);
              }
            });
  }

  public static LoadBalanceStats instance() {
    return INSTANCE;
  }

  public ServiceStats get(String serviceId) {
    try {
      return cache.get(serviceId);
    } catch (ExecutionException e) {
      ServiceStats stats = new ServiceStats(serviceId);
      cache.asMap().putIfAbsent(serviceId, stats);
      return cache.asMap().get(serviceId);
    }
  }
}