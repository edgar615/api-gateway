package com.edgar.direwolves.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * 每个API的权限校验方式，每个API可以有多种校验方式.
 * <p/>
 * 如果apiName和authType相同，则认为该对象是同一个对象。
 *
 * @author Edgar  Date 2016/9/12
 */
class AuthDefinitionImpl implements AuthDefinition {

    /**
     * api名称
     */
    private final String apiName;

    /**
     * 权限类型
     */
    private final AuthType authType;

    AuthDefinitionImpl(String apiName, AuthType authType) {
        Preconditions.checkNotNull(apiName, "apiName can not be null");
        Preconditions.checkNotNull(authType, "authType can not be null");
        this.apiName = apiName;
        this.authType = authType;
    }


    @Override
    public String apiName() {
        return apiName;
    }

    @Override
    public AuthType authType() {
        return authType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("AuthDefinition")
                .add("apiName", apiName)
                .add("authType", authType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthDefinitionImpl that = (AuthDefinitionImpl) o;

        if (!apiName.equals(that.apiName)) return false;
        if (authType != that.authType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = apiName.hashCode();
        result = 31 * result + authType.hashCode();
        return result;
    }
}
