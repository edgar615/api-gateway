package com.edgar.direwolves.loadbalance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by edgar on 17-7-29.
 */
public class ServerStats {

  private boolean circuitBreakerTripped;

  private AtomicInteger activeRequests;

  private AtomicInteger weight;

  private AtomicInteger effectiveWeight;

//  private AtomicLong activeConnectionsLimit

  public boolean isCircuitBreakerTripped() {
    return circuitBreakerTripped;
  }

  public void setCircuitBreakerTripped(boolean circuitBreakerTripped) {
    this.circuitBreakerTripped = circuitBreakerTripped;
  }
}
