package com.github.edgar615.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Edgar on 2017/12/4.
 *
 * @author Edgar  Date 2017/12/4
 */
public abstract class AbstractTransformerFilter implements Filter {

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 15000;
  }

  protected void setGlobalTransformer(JsonObject config, AtomicReference<RequestTransformer> reference) {
    if (config.getValue("request.transformer") instanceof JsonObject) {
      JsonObject jsonObject = config.getJsonObject("request.transformer", new JsonObject());
      if (!jsonObject.isEmpty()) {
        RequestTransformer  globalTransfomer = RequestTransformer.create("global");
        RequestTransfomerConverter.fromJson(jsonObject, globalTransfomer);
        reference.set(globalTransfomer);
      }
    }

  }

  protected Multimap<String, String> tranformerParams(Multimap<String, String> params,
                                                      RequestTransformer transformer) {
    Multimap<String, String> newParams = ArrayListMultimap.create(params);
    transformer.paramRemoved().forEach(h -> newParams.removeAll(h));
    transformer.paramReplaced().forEach(entry -> {
      if (newParams.containsKey(entry.getKey())) {
        Collection<String> values = newParams.removeAll(entry.getKey());
        newParams.putAll(entry.getValue(), values);
      }
    });
    transformer.paramAdded().forEach(
            entry -> newParams.put(entry.getKey(), entry.getValue()));
    return newParams;
  }

  protected Multimap<String, String> tranformerHeaders(Multimap<String, String> headers,
                                                       RequestTransformer transformer) {
    Multimap<String, String> newHeader = ArrayListMultimap.create(headers);
    transformer.headerRemoved().forEach(h -> newHeader.removeAll(h));
    transformer.headerReplaced().forEach(entry -> {
      if (newHeader.containsKey(entry.getKey())) {
        Collection<String> values = newHeader.removeAll(entry.getKey());
        newHeader.putAll(entry.getValue(), values);
      }
    });
    transformer.headerAdded().forEach(
            entry -> newHeader.put(entry.getKey(), entry.getValue()));
    return newHeader;
  }

  protected JsonObject tranformerBody(final JsonObject body,
                                      RequestTransformer transformer) {
    final JsonObject newBody = new JsonObject();
    if (body != null) {
      newBody.mergeIn(body.copy());
    }
    transformer.bodyRemoved().forEach(b -> newBody.remove(b));
    transformer.bodyReplaced().forEach(entry -> {
      if (newBody.containsKey(entry.getKey())) {
        Object value = newBody.remove(entry.getKey());
        newBody.put(entry.getValue(), value);
      }
    });
    transformer.bodyAdded().forEach(entry -> newBody.put(entry.getKey(), entry.getValue()));
    return newBody;
  }
}
