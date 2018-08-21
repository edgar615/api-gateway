package com.github.edgar615.gateway.plugin.transformer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP Transformer.
 *
 * @author Edgar  Date 2016/9/12
 */
class RequestTransformerImpl implements RequestTransformer {

    /**
     * 服务名
     */
    private final String name;

    private final List<String> headerRemove = new ArrayList<>();

    private final List<Map.Entry<String, String>> headerAdd = new ArrayList<>();

    private final List<Map.Entry<String, String>> headerReplace = new ArrayList<>();

    private final List<String> bodyRemove = new ArrayList<>();

    private final List<Map.Entry<String, String>> bodyAdd = new ArrayList<>();

    private final List<Map.Entry<String, String>> bodyReplace = new ArrayList<>();

    private final List<String> paramRemove = new ArrayList<>();

    private final List<Map.Entry<String, String>> paramAdd = new ArrayList<>();

    private final List<Map.Entry<String, String>> paramReplace = new ArrayList<>();

    RequestTransformerImpl(String name) {
        Preconditions.checkNotNull(name, "name can not be null");
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("ResponseTransformer")
                .add("name", name)
                .add("headerRemove", headerRemove)
                .add("headerReplace", headerReplace)
                .add("headerAdd", headerAdd)
                .add("bodyRemove", bodyRemove)
                .add("bodyReplace", bodyReplace)
                .add("bodyAdd", bodyAdd)
                .add("bodyRemove", paramRemove)
                .add("bodyReplace", paramReplace)
                .add("bodyAdd", paramAdd)
                .toString();
    }

    @Override
    public List<Map.Entry<String, String>> bodyReplaced() {
        return bodyReplace;
    }

    @Override
    public List<Map.Entry<String, String>> bodyAdded() {
        return bodyAdd;
    }

    @Override
    public List<String> bodyRemoved() {
        return bodyRemove;
    }

    @Override
    public RequestTransformer addBody(String key, String value) {
        this.bodyAdd.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public RequestTransformer replaceBody(String key, String value) {
        this.bodyReplace.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public RequestTransformer removeBody(String key) {
        this.bodyRemove.add(key);
        return this;
    }

    @Override
    public List<Map.Entry<String, String>> headerReplaced() {
        return headerReplace;
    }

    @Override
    public List<Map.Entry<String, String>> headerAdded() {
        return headerAdd;
    }

    @Override
    public List<String> headerRemoved() {
        return headerRemove;
    }

    @Override
    public RequestTransformer addHeader(String key, String value) {
        this.headerAdd.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public RequestTransformer replaceHeader(String key, String value) {
        this.headerReplace.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public RequestTransformer removeHeader(String key) {
        this.headerRemove.add(key);
        return this;
    }

    @Override
    public List<Map.Entry<String, String>> paramReplaced() {
        return paramReplace;
    }

    @Override
    public List<Map.Entry<String, String>> paramAdded() {
        return paramAdd;
    }

    @Override
    public List<String> paramRemoved() {
        return paramRemove;
    }

    @Override
    public RequestTransformer addParam(String key, String value) {
        this.paramAdd.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public RequestTransformer replaceParam(String key, String value) {
        this.paramReplace.add(Maps.immutableEntry(key, value));
        return this;
    }

    @Override
    public RequestTransformer removeParam(String key) {
        this.paramRemove.add(key);
        return this;
    }
}
