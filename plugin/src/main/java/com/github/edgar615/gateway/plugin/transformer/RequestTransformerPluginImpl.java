package com.github.edgar615.gateway.plugin.transformer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-10-22.
 */
public class RequestTransformerPluginImpl implements RequestTransformerPlugin {

    private final List<RequestTransformer> transformers = new ArrayList<>();

    @Override
    public List<RequestTransformer> transformers() {
        return ImmutableList.copyOf(transformers);
    }

    @Override
    public RequestTransformerPlugin addTransformer(RequestTransformer transformer) {
        removeTransformer(transformer.name());
        this.transformers.add(transformer);
        return this;
    }

    @Override
    public RequestTransformer transformer(String name) {
        List<RequestTransformer> list = this.transformers.stream()
                .filter(t -> t.name().equalsIgnoreCase(name))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public RequestTransformerPlugin removeTransformer(String name) {
        RequestTransformer requestTransformer = transformer(name);
        if (requestTransformer != null) {
            this.transformers.remove(requestTransformer);
        }
        return this;
    }

    @Override
    public RequestTransformerPlugin clear() {
        this.transformers.clear();
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper("RequestTransformerPlugin")
                .add("transformers", transformers)
                .toString();
    }
}
