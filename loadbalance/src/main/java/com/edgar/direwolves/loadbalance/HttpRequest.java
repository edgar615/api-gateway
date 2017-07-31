package com.edgar.direwolves.loadbalance;

import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.rpc.RpcRequest;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
public interface HttpRequest  {

  String id();

  String path();

  int timeout();

  JsonObject body();

  HttpMethod method();

  Record record();

  Multimap<String, String> params();

  Multimap<String, String> headers();

}
