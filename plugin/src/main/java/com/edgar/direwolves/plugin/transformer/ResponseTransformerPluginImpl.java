package com.edgar.direwolves.plugin.transformer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP的远程调用定义.
 *
 * @author Edgar  Date 2016/9/12
 */
class ResponseTransformerPluginImpl implements ResponseTransformerPlugin {

  private final List<String> headerRemove = new ArrayList<>();

  private final List<Map.Entry<String, String>> headerAdd = new ArrayList<>();

  private final List<Map.Entry<String, String>> headerReplace = new ArrayList<>();

  private final List<String> bodyRemove = new ArrayList<>();

  private final List<Map.Entry<String, String>> bodyAdd = new ArrayList<>();

  private final List<Map.Entry<String, String>> bodyReplace = new ArrayList<>();

  ResponseTransformerPluginImpl() {
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ResponseTransformer")
            .add("headerRemove", headerRemove)
            .add("headerReplace", headerReplace)
            .add("headerAdd", headerAdd)
            .add("bodyRemove", bodyRemove)
            .add("bodyReplace", bodyReplace)
            .add("bodyAdd", bodyAdd)
            .toString();
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
  public ResponseTransformerPlugin addBody(String key, String value) {
    this.bodyAdd.add(Maps.immutableEntry(key, value));
    return this;
  }

  @Override
  public ResponseTransformerPlugin removeBody(String key) {
    this.bodyRemove.add(key);
    return this;
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
  public ResponseTransformerPlugin addHeader(String key, String value) {
    this.headerAdd.add(Maps.immutableEntry(key, value));
    return this;
  }

  @Override
  public ResponseTransformerPlugin removeHeader(String key) {
    this.headerRemove.add(key);
    return this;
  }

}
