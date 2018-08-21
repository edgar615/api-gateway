package com.github.edgar615.gateway.core.definition;

/**
 * Created by Edgar on 2017/1/6.
 *
 * @author Edgar  Date 2017/1/6
 */
public class MockPlugin implements ApiPlugin {
    @Override
    public String name() {
        return MockPlugin.class.getSimpleName();
    }
}
