package com.edgar.direwolves.loadbalance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by edgar on 17-7-29.
 */
public class ServiceStats {

  private final String serviceId;

  private boolean circuitBreakerTripped;

  private AtomicInteger activeRequests = new AtomicInteger();

  private AtomicInteger weight;

  private AtomicInteger effectiveWeight;

  public ServiceStats(String serviceId) {this.serviceId = serviceId;}

//  private AtomicLong activeConnectionsLimit

  public boolean isCircuitBreakerTripped() {
    return circuitBreakerTripped;
  }

  public ServiceStats setCircuitBreakerTripped(boolean circuitBreakerTripped) {
    this.circuitBreakerTripped = circuitBreakerTripped;
    return this;
  }

  public ServiceStats incActiveRequests() {
    activeRequests.incrementAndGet();
    return this;
  }

  public ServiceStats decActiveRequests() {
    activeRequests.decrementAndGet();
    return this;
  }
}
