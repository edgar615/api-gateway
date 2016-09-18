package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.AuthDefinition;
import com.edgar.direwolves.definition.AuthDefinitionRegistry;
import com.edgar.direwolves.definition.AuthType;

import java.util.List;

/**
 * Created by edgar on 16-9-18.
 */
public class AppKeyFilter implements Filter {
    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean shouldFilter(String apiName, ApiContext apiContext) {
        List<AuthDefinition> definitions = AuthDefinitionRegistry.create()
                .filter(apiName, AuthType.APP_KEY);
        return !definitions.isEmpty();
    }

    @Override
    public void doFilter(String apiName, ApiContext apiContext) {
        //JWT校验
    }
}
