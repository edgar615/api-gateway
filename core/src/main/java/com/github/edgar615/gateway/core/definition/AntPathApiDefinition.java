package com.github.edgar615.gateway.core.definition;

import com.google.common.base.Preconditions;

import io.vertx.core.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * ant匹配规则的API定义.
 *
 * @author Edgar  Date 2018/1/11
 */
class AntPathApiDefinition extends ApiDefinitionImpl {

    /**
     * 忽略的规则，如果匹配这个规则，将不再进行ant匹配.
     */
    private final List<String> ignoredPatterns = new ArrayList<>();

    AntPathApiDefinition(String name, HttpMethod method, String path,
                         List<Endpoint> endpoints) {
        super(name, method, path, endpoints);
    }

    public AntPathApiDefinition addIgnoredPattern(String pattern) {
        Preconditions.checkNotNull(pattern);
        this.ignoredPatterns.add(pattern);
        return this;
    }

    public List<String> ignoredPatterns() {
        return ignoredPatterns;
    }

}
