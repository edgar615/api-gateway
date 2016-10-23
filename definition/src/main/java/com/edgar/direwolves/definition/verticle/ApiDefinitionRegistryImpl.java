package com.edgar.direwolves.definition.verticle;

import com.edgar.direwolves.definition.ApiDefinition;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 路由映射关系的注册表.
 */
class ApiDefinitionRegistryImpl implements ApiDefinitionRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionRegistryImpl.class);

  private static final ApiDefinitionRegistry INSTANCE = new ApiDefinitionRegistryImpl();

  private final List<ApiDefinition> definitions = new ArrayList<>();

  private final Lock rl;

  private final Lock wl;

  private ApiDefinitionRegistryImpl() {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    this.rl = lock.readLock();
    this.wl = lock.writeLock();
  }

  static ApiDefinitionRegistry instance() {
    return INSTANCE;
  }


  /**
   * 获取路由映射的列表.
   *
   * @return ApiMapping的不可变集合.
   */
  @Override
  public Set<ApiDefinition> getDefinitions() {
    try {
      rl.lock();
      return ImmutableSet.copyOf(definitions);
    } finally {
      rl.unlock();
    }
  }

  /**
   * 向注册表中添加一个路由映射.
   * 映射表中name必须唯一.重复添加的数据会覆盖掉原来的映射.
   *
   * @param apiDefinition 路由映射.
   */
  @Override
  public void add(ApiDefinition apiDefinition) {
    Preconditions.checkNotNull(apiDefinition, "apiDefinition is null");
    try {
      wl.lock();
      remove(apiDefinition.name());
      this.definitions.add(apiDefinition);
    } finally {
      wl.unlock();
    }
    LOGGER.debug("add ApiDefinition {}", apiDefinition);
  }

  /**
   * 根据name删除符合的路由映射.
   * 如果name=null，会查找所有的权限映射.
   * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
   * *user会查询所有以user结尾对name,如add_user.
   * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
   *
   * @param name API名称
   */
  @Override
  public void remove(String name) {
    List<ApiDefinition> apiDefinitions = filter(name);
    if (apiDefinitions != null && !apiDefinitions.isEmpty()) {
      try {
        wl.lock();
        this.definitions.removeAll(apiDefinitions);
      } finally {
        wl.unlock();
      }
      LOGGER.debug("remove ApiDefinition {}", apiDefinitions);
    }
  }

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
  @Override
  public List<ApiDefinition> filter(String name) {
    Predicate<ApiDefinition> predicate = definition -> true;
    predicate = namePredicate(name, predicate);
    try {
      rl.lock();
      return this.definitions.stream().filter(predicate).collect(Collectors.toList());
    } finally {
      rl.unlock();
    }
  }


  private Predicate<ApiDefinition> namePredicate(String name, Predicate<ApiDefinition> predicate) {
    if (!Strings.isNullOrEmpty(name) && !"*".equals(name)) {
      boolean isStartsWith = false;
      boolean isEndsWith = false;
      String checkName = name;
      if (name.endsWith("*")) {
        checkName = checkName.substring(0, name.length() - 1);
        isStartsWith = true;
      }
      if (name.startsWith("*")) {
        checkName = checkName.substring(1);
        isEndsWith = true;
      }
      final String finalCheckName = checkName.toLowerCase();
      final boolean finalIsStartsWith = isStartsWith;
      final boolean finalIsEndsWith = isEndsWith;
      predicate = predicate.and(definition -> {
        boolean startCheck = false;
        boolean endCheck = false;
        boolean equalsCheck = false;
        if (finalIsStartsWith) {
          startCheck = definition.name().toLowerCase().startsWith(finalCheckName);
        }
        if (finalIsEndsWith) {
          endCheck = definition.name().toLowerCase().endsWith(finalCheckName);
        }
        equalsCheck = finalCheckName.equalsIgnoreCase(definition.name());
        return equalsCheck || endCheck || startCheck;
      });
    }
    return predicate;
  }
}