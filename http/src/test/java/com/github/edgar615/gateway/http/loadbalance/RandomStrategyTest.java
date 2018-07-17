package com.github.edgar615.gateway.http.loadbalance;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

/**
 * Created by edgar on 17-5-6.
 */
public class RandomStrategyTest extends StrategyTest {

  @Test
  public void testRandom() {
    ChooseStrategy chooseStrategy = ChooseStrategy.random();
    List<String> selected = select3000(chooseStrategy);
    Assert.assertEquals(3, new HashSet<>(selected).size());
    long aSize = selected.stream()
            .filter(i -> "a".equals(i))
            .count();
    long bSize = selected.stream()
            .filter(i -> "b".equals(i))
            .count();
    long cSize = selected.stream()
            .filter(i -> "c".equals(i))
            .count();
    Assert.assertNotEquals(aSize, 1000);
    Assert.assertNotEquals(bSize, 1000);
    Assert.assertNotEquals(cSize, 1000);
  }

}
