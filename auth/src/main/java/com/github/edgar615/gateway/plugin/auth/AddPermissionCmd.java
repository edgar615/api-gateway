package com.github.edgar615.gateway.plugin.auth;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.cmd.ApiSubCmd;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.Validations;
import io.vertx.core.json.JsonObject;

/**
 * 设置鉴权的命令
 * <p>
 * 命令字:plugin.permission.add
 * 参数 permission:权限值
 *
 * @author Edgar  Date 2017/1/20
 */
public class AddPermissionCmd implements ApiSubCmd {

    private final Multimap<String, Rule> rules = ArrayListMultimap.create();

    public AddPermissionCmd() {
        rules.put("permission", Rule.required());
    }

    @Override
    public String cmd() {
        return "plugin.permission.add";
    }

    @Override
    public void handle(ApiDefinition definition, JsonObject jsonObject) {
        Validations.validate(jsonObject.getMap(), rules);
        String scope = jsonObject.getString("permission");
        PermissionPlugin plugin = PermissionPlugin.create(scope);
        definition.addPlugin(plugin);

    }

}
