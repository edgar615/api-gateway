package com.edgar.direwolves.dispatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.MultiMap;

import java.util.Set;
import java.util.function.Function;

/**
 * 将vertx的MultiMap转换为Guava的Multimap
 *
 * @author Edgar  Date 2016/8/24
 */
@Deprecated
public class MultiMapToMultimap implements Function<MultiMap, Multimap<String, String>> {

  private static final MultiMapToMultimap INSTANCE = new MultiMapToMultimap();

  private MultiMapToMultimap() {

  }

  public static Function<MultiMap, Multimap<String, String>> instance() {
    return INSTANCE;
  }

  @Override
  public Multimap<String, String> apply(MultiMap multiMap) {
    Multimap<String, String> guavaMultimap = ArrayListMultimap.create();
    Set<String> names = multiMap.names();
    for (String paramName : names) {
      guavaMultimap.putAll(paramName, multiMap.getAll(paramName));
    }
    return guavaMultimap;
  }
}