package com.edgar.direwolves.definition;

import java.util.List;
import java.util.Set;

/**
 * API权限的映射关系的注册表.
 *
 * @author Edgar  Date 2016/9/14
 */
public interface AuthDefinitionRegistry {

    static AuthDefinitionRegistry create() {
        return AuthDefinitionRegistryImpl.instance();
    }

    /**
     * 获取API权限的映射关系的列表.
     *
     * @return AuthDefinition的不可变集合.
     */
    Set<AuthDefinition> getDefinitions();

    /**
     * 向注册表中添加一个权限映射.
     * 映射表中apiName和authType的组合必须唯一.重复添加的数据会覆盖掉原来的映射.
     *
     * @param definition 权限映射.
     */
    void add(AuthDefinition definition);

    /**
     * 根据组合条件删除映射.
     * 如果apiName=get_device, authType = AuthType.JWT，会从注册表中删除所有apiName=get_device, authType = AuthType.JWT的映射.
     * 如果apiName=get_device, authType = null，会从注册表中删除所有apiName=get_device的映射.
     * 如果apiName=null, authType = AuthType.JWT，会从注册表中删除所有authType = AuthType.JWT的映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param apiName  API名称
     * @param authType 权限类型
     */
    void remove(String apiName, AuthType authType);

    /**
     * 根据组合条件查询映射.
     * 如果apiName=get_device, authType = AuthType.JWT，会从注册表中查询apiName=get_device, authType = AuthType.JWT的映射.
     * 如果apiName=get_device, authType = null，会从注册表中查询所有apiName=get_device的映射.
     * 如果apiName=null, authType = AuthType.JWT，会从注册表中查询所有authType = AuthType.JWT的映射.
     * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
     * *user会查询所有以user结尾对name,如add_user.
     * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
     *
     * @param apiName  API名称
     * @param authType 权限类型
     * @return AuthDefinition的集合
     */
    List<AuthDefinition> filter(String apiName, AuthType authType);
}
