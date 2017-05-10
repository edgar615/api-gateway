package com.edgar.direwolves.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于权重的随机轮询策略.
 * <p>
 * 算法来自nginx: https://github.com/phusion/nginx/commit/27e94984486058d73157038f7950a0a36ecc6e35
 * <p>
 * <p>
 * Upstream: smooth weighted round-robin balancing.
 * <p>
 * For edge case weights like { 5, 1, 1 } we now produce { a, a, b, a, c, a, a }
 * sequence instead of { c, b, a, a, a, a, a } produced previously.
 * <p>
 * Algorithm is as follows: on each peer selection we increase current_weight
 * of each eligible peer by its weight, select peer with greatest current_weight
 * and reduce its current_weight by total number of weight points distributed
 * among peers.
 * <p>
 * In case of { 5, 1, 1 } weights this gives the following sequence of
 * current_weight's:
 * <p>
 * a  b  c
 * 0  0  0  (initial state)
 * <p>
 * 5  1  1  (a selected)
 * -2  1  1
 * <p>
 * 3  2  2  (a selected)
 * -4  2  2
 * <p>
 * 1  3  3  (b selected)
 * 1 -4  3
 * <p>
 * 6 -3  4  (a selected)
 * -1 -3  4
 * <p>
 * 4 -2  5  (c selected)
 * 4 -2 -2
 * <p>
 * 9 -1 -1  (a selected)
 * 2 -1 -1
 * <p>
 * 7  0  0  (a selected)
 * 0  0  0
 * <p>
 * To preserve weight reduction in case of failures the effective_weight
 * variable was introduced, which usually matches peer's weight, but is
 * reduced temporarily on peer failures.
 * Created by edgar on 17-5-6.
 */
class WeightRoundbinStrategy implements ProviderStrategy {

  @Override
  public ServiceInstance get(List<ServiceInstance> instances) {
    ServiceInstance instance = compute(instances);
    //重新计算weight
    return instance;
  }

  public ServiceInstance compute(List<ServiceInstance> instances) {
    int total = instances.stream()
            .map(r -> r.weight())
            .reduce(0, (i1, i2) -> i1 + i2);

    instances.stream()
            .forEach(i -> i.incEffectiveWeight(i.weight()));
    ServiceInstance instance = instances.stream()
            .max((o1, o2) -> o1.effectiveWeight() - o2.effectiveWeight())
            .get();
    Collections.sort(new ArrayList<>(instances), (o1, o2) -> o1.weight() - o2.weight());
    return instance.decEffectiveWeight(total);
  }

}
