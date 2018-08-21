package com.github.edgar615.gateway.filter;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.AntPathMatcher;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 该filter将API定义中的ant分隔与请求路径做匹配，匹配路径保存在上下文
 * <p>
 * 所有的参数名将保存在上下文变量中，可以通过$var.extractPath变量来获得。
 * <p>
 * 示例:API定义的路径为/devices/**，请求的路径为/devices/1，那么对应的参数名为extractPath的值就为1
 * <p>
 * Created by edgar on 18-1-11.
 */
public class AntPathParamFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntPathParamFilter.class);

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 2000;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.apiDefinition() != null
               && apiContext.apiDefinition().antStyle();
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        AntPathMatcher matcher = new AntPathMatcher.Builder().build();
        String pattern = apiContext.apiDefinition().path();
        String extractPath = matcher.extractPathWithinPattern(pattern, apiContext.path());
        apiContext.addVariable("extractPath", extractPath);
        completeFuture.complete(apiContext);
    }

}
