package com.edgar.direwolves.definition;

/**
 * 每个API的权限校验方式，每个API可以有多种校验方式.
 * <p/>
 * 如果apiName和authType相同，则认为该对象是同一个对象。
 *
 * @author Edgar  Date 2016/9/12
 */
public interface AuthDefinition {

    static AuthDefinition create(String apiName, AuthType authType) {
        return new AuthDefinitionImpl(apiName, authType);
    }

    /**
     * @return api名称
     */
    String apiName();

    /**
     * @return 权限类型
     */
    AuthType authType();
}
