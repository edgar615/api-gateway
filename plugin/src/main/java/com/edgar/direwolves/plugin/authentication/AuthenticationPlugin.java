package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;

import java.util.List;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public interface AuthenticationPlugin extends ApiPlugin {

  /**
   * 增加一个权限认证类型.
   *
   * @param authentication 权限类型,目前仅支持jwt
   * @return
   */
  AuthenticationPlugin add(String authentication);

  /**
   * 删除一个权限认证类型.
   *
   * @param authentication 权限类型,目前仅支持jwt
   * @return
   */
  AuthenticationPlugin remove(String authentication);

  /**
   * 删除所有的权限认证.
   *
   * @return
   */
  AuthenticationPlugin clear();

  /**
   * @return 权限认证列表
   */
  List<String> authentications();

  @Override
  default String name() {
    return AuthenticationPlugin.class.getSimpleName();
  }

}
