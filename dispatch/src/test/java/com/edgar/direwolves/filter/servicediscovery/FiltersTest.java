package com.edgar.direwolves.filter.servicediscovery;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import com.edgar.direwolves.filter.ApiFindFilter;
import com.edgar.direwolves.filter.PathParamFilter;
import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Created by edgar on 17-1-4.
 */
public class FiltersTest {

  @Test
  public void testSort() {
    Vertx vertx = Vertx.vertx();
    JsonObject config = new JsonObject();
    PathParamFilter pathParamFilter = new PathParamFilter();
    ApiFindFilter apiFindFilter = new ApiFindFilter(vertx, config);
    List<Filter> filterList = Lists.newArrayList(pathParamFilter, apiFindFilter);
    Collections.sort(filterList, (o1, o2) -> o1.order() - o2.order());
    System.out.println(filterList);
  }
}
