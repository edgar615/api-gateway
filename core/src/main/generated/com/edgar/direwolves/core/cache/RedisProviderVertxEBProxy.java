/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package com.edgar.direwolves.core.cache;

import com.edgar.direwolves.core.cache.RedisProvider;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import com.edgar.direwolves.core.cache.RedisProvider;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class RedisProviderVertxEBProxy implements RedisProvider {

  private Vertx _vertx;
  private String _address;
  private DeliveryOptions _options;
  private boolean closed;

  public RedisProviderVertxEBProxy(Vertx vertx, String address) {
    this(vertx, address, null);
  }

  public RedisProviderVertxEBProxy(Vertx vertx, String address, DeliveryOptions options) {
    this._vertx = vertx;
    this._address = address;
    this._options = options;
    try {
      this._vertx.eventBus().registerDefaultCodec(ServiceException.class,
          new ServiceExceptionMessageCodec());
    } catch (IllegalStateException ex) {}
  }

  public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("key", key);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "get");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void set(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("key", key);
    _json.put("value", value);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "set");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void setex(String key, JsonObject value, int expires, Handler<AsyncResult<Void>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("key", key);
    _json.put("value", value);
    _json.put("expires", expires);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "setex");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void delete(String key, Handler<AsyncResult<Void>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("key", key);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "delete");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void scriptLoad(String script, Handler<AsyncResult<String>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("script", script);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "scriptLoad");
    _vertx.eventBus().<String>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void evalsha(String sha1, List<String> keys, List<String> args, Handler<AsyncResult<JsonArray>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("sha1", sha1);
    _json.put("keys", new JsonArray(keys));
    _json.put("args", new JsonArray(args));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "evalsha");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void eval(String script, List<String> keys, List<String> args, Handler<AsyncResult<JsonArray>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("script", script);
    _json.put("keys", new JsonArray(keys));
    _json.put("args", new JsonArray(args));
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "eval");
    _vertx.eventBus().<JsonArray>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void acquireToken(JsonArray rules, Handler<AsyncResult<JsonObject>> handler) {
    if (closed) {
      handler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("rules", rules);
    DeliveryOptions _deliveryOptions = (_options != null) ? new DeliveryOptions(_options) : new DeliveryOptions();
    _deliveryOptions.addHeader("action", "acquireToken");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        handler.handle(Future.failedFuture(res.cause()));
      } else {
        handler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }


  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)(int)jobj);
    }
    return list;
  }

  private Set<Character> convertToSetChar(JsonArray arr) {
    Set<Character> set = new HashSet<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      set.add((char)(int)jobj);
    }
    return set;
  }

  private <T> Map<String, T> convertMap(Map map) {
    if (map.isEmpty()) { 
      return (Map<String, T>) map; 
    } 
     
    Object elem = map.values().stream().findFirst().get(); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (Map<String, T>) map; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return ((Map<String, T>) map).entrySet() 
       .stream() 
       .collect(Collectors.toMap(Map.Entry::getKey, converter::apply)); 
    } 
  }
  private <T> List<T> convertList(List list) {
    if (list.isEmpty()) { 
          return (List<T>) list; 
        } 
     
    Object elem = list.get(0); 
    if (!(elem instanceof Map) && !(elem instanceof List)) { 
      return (List<T>) list; 
    } else { 
      Function<Object, T> converter; 
      if (elem instanceof List) { 
        converter = object -> (T) new JsonArray((List) object); 
      } else { 
        converter = object -> (T) new JsonObject((Map) object); 
      } 
      return (List<T>) list.stream().map(converter).collect(Collectors.toList()); 
    } 
  }
  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>(convertList(list));
  }
}