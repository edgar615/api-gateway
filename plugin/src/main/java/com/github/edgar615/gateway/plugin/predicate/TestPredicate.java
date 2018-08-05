package com.github.edgar615.gateway.plugin.predicate;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.dispatch.ApiContext;

import java.util.function.BiPredicate;

public class TestPredicate implements BiPredicate<ApiContext, ApiDefinition> {
  @Override
  public boolean test(ApiContext apiContext, ApiDefinition apiDefinition) {
    return false;
  }
}
