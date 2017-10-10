package com.github.edgar615.direwolves.verticle;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import io.vertx.core.http.HttpMethod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 路由映射关系的注册表.
 * <p>
 * ApiDefinitionRegistry的实现是一个单例对象。
 * 每个网关应用都会保存一个ApiDefinitionRegistry对象。用来实现Api的缓存，避免频繁远程调用的损耗。
 * 所有api的维护都是通过ApiDiscovery中的eventbus接受backend发出的修改事件来同步更新。
 * Created by edgar on 16-9-13.
 */
@Deprecated
public interface ApiDefinitionRegistry {

  /**
   * 获取路由映射的列表.
   *
   * @return ApiMapping的不可变集合.
   */
  Set<ApiDefinition> getDefinitions();

  /**
   * 向注册表中添加一个路由映射.
   * 映射表中name必须唯一.重复添加的数据会覆盖掉原来的映射.
   *
   * @param apiDefinition 路由映射.
   */
  void add(ApiDefinition apiDefinition);

  /**
   * 根据name删除符合的路由映射.
   * 如果name=null，会查找所有的权限映射.
   * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
   * *user会查询所有以user结尾对name,如add_user.
   * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
   *
   * @param name API名称
   */
  void remove(String name);

  /**
   * 根据name查找所有的路由映射.
   * 如果name=null，会查找所有的权限映射.
   * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
   * *user会查询所有以user结尾对name,如add_user.
   * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
   *
   * @param name API名称
   * @return ApiDefinition的集合
   */
  List<ApiDefinition> filter(String name);

  static ApiDefinitionRegistry create() {
    return ApiDefinitionRegistryImpl.instance();
  }

  /**
   * 根据请求方法和请求地址查找API定义.
   * 只有当method相同，且path符合ApiDefinition的正则表达式才认为二者匹配.
   *
   * @param method 请求方法
   * @param path   路径
   * @return ApiDefinition的集合
   */
  default List<ApiDefinition> match(HttpMethod method, String path) {
    return filter(null)
            .stream()
            .filter(definition -> definition.match(method, path))
            .collect(Collectors.toList());
  }
}
