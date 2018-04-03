package com.github.edgar615.direvolves.plugin.gray;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;

/**
 * Created by Edgar on 2018/2/9.
 *
 * @author Edgar  Date 2018/2/9
 */
public class GrayFilter implements Filter {

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 12500;
  }


  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return false;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

  }
}
