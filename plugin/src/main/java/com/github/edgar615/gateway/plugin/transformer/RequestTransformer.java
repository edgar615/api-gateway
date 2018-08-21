package com.github.edgar615.gateway.plugin.transformer;

/**
 * request的转换规则.
 *
 * @author Edgar  Date 2016/10/8
 */
public interface RequestTransformer extends BodyTransfomer, HeaderTransfomer, ParamTransfomer {

    /**
     * @return endpoint的名称
     */
    String name();

    static RequestTransformer create(String name) {
        return new RequestTransformerImpl(name);
    }

}
