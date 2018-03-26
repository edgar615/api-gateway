package com.github.edgar615.direvolves.plugin.gray;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2018/2/9.
 *
 * @author Edgar  Date 2018/2/9
 */
public class GrayFilter implements Filter {

  private List<ServiceSplitter> serviceSplitters = new ArrayList<>();

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
