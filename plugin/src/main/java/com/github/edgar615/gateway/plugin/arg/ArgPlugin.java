package com.github.edgar615.gateway.plugin.arg;

import java.util.List;

/**
 * 参数校验的接口.
 * Created by edgar on 16-10-23.
 */
public interface ArgPlugin {

    /**
     * @return 参数列表
     */
    List<Parameter> parameters();

    /**
     * 增加一个参数
     *
     * @param parameter 参数
     * @return ArgPlugin
     */
    ArgPlugin add(Parameter parameter);

    /**
     * 删除一个参数
     *
     * @param name 参数名
     * @return ArgPlugin
     */
    ArgPlugin remove(String name);

    /**
     * @param name 参数名
     * @return　参数
     */
    Parameter parameter(String name);

    /**
     * 删除所有参数.
     *
     * @return ArgPlugin
     */
    ArgPlugin clear();
}
