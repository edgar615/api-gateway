package com.edgar.direwolves.loadbalance;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
public class LoadBalanceStats {

  private final LoadingCache<String, ServiceStats> cache;

  public LoadBalanceStats() {
    this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
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

  public ServiceStats getServiceStat(String serviceId) {
    try {
      return cache.get(serviceId);
    } catch (ExecutionException e) {
      ServiceStats stats = new ServiceStats(serviceId);
      cache.asMap().putIfAbsent(serviceId, stats);
      return cache.asMap().get(serviceId);
    }
  }
}
