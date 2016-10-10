package com.edgar.direwolves.dispatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.filter.Filters;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class FiltersTest {

  Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
  }

  @Test
  public void tsetNoJwtHeader(TestContext testContext) {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", ArrayListMultimap
            .create(), null, null);

    Filters filters = Filters.instance();
    filters.init(vertx);

    Task<ApiContext> task = filters.doFilter(apiContext);
    task.andThen(context -> {
      testContext.fail();
    }).onFailure(throwable -> {
      testContext.assertTrue(throwable instanceof SystemException);
      SystemException ex = (SystemException) throwable;
      testContext.assertEquals(1021, ex.getErrorCode().getNumber());
    });
  }

  @Test
  public void testJwtHeader(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + "abc");
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    Filters filters = Filters.instance();
    filters.init(vertx);

    Task<ApiContext> task = filters.doFilter(apiContext);
    task.andThen(context -> {
      testContext.fail();
    }).onFailure(throwable -> {
      testContext.assertTrue(throwable instanceof SystemException);
      SystemException ex = (SystemException) throwable;
      testContext.assertEquals(DefaultErrorCode.NO_AUTHORITY.getNumber(),
                               ex.getErrorCode().getNumber());
    });
  }
}
