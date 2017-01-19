package com.edgar.direwolves.cmd;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.Validations;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 删除API.
 * 命令字: api.delete
 * <p>
 * 参数 {name : 要删除的API名称}
 * 如果name=null，会查找所有的权限映射.
 * name支持两种通配符 user*会查询所有以user开头的name，如user.add．
 * *user会查询所有以user结尾对name,如add_user.
 * *表示所有.**也表示所有.但是***表示中间有一个*的字符串,如user*add
 *
 * @author Edgar  Date 2017/1/19
 */
class DeleteApiCmd implements ApiCmd {

  private final Multimap<String, Rule> rules = ArrayListMultimap.create();

  DeleteApiCmd() {
    rules.put("name", Rule.required());
  }

  @Override
  public String cmd() {
    return "api.delete";
  }

  @Override
  public Future<JsonObject> doHandle(JsonObject jsonObject) {
    Validations.validate(jsonObject.getMap(), rules);
    String name = jsonObject.getString("name");
    ApiDefinitionRegistry.create().remove(name);
    return Future.succeededFuture(succeedResult());
  }
}
