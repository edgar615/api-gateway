package com.edgar.direwolves.plugin.transformer;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-10-22.
 */
public class ResponseTransformerPluginImpl implements ResponseTransformerPlugin {

  private final List<ResponseTransformer> transformers = new ArrayList<>();

  @Override
  public List<ResponseTransformer> transformers() {
    return ImmutableList.copyOf(transformers);
  }

  @Override
  public ResponseTransformerPlugin addTransformer(ResponseTransformer transformer) {
    removeTransformer(transformer.name());
    this.transformers.add(transformer);
    return this;
  }

  @Override
  public ResponseTransformer transformer(String name) {
    List<ResponseTransformer> list = this.transformers.stream()
        .filter(t -> t.name().equalsIgnoreCase(name))
        .collect(Collectors.toList());
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public ResponseTransformerPlugin removeTransformer(String name) {
    ResponseTransformer requestTransformer = transformer(name);
    if (requestTransformer != null) {
      this.transformers.remove(requestTransformer);
    }
    return this;
  }

  @Override
  public ResponseTransformerPlugin clear() {
    this.transformers.clear();
    return this;
  }
}
