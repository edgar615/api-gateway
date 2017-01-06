package com.edgar.direwolves.core.rpc;

import io.vertx.core.Future;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public interface RpcHandler {

//  List<RpcHandlerFactory> factories =
//      ImmutableList.copyOf(ServiceLoader.load(RpcHandlerFactory.class));
//
//  ConcurrentMap<String, RpcHandler> cache = new ConcurrentHashMap<>();
//
//  RpcHandler failedRpcHandler = new FailureRpcHandler("Undefined Rpc");
//  ReadWriteLock lock = new ReentrantReadWriteLock();
//
//  static RpcHandler create(String type, Vertx vertx, JsonObject config) {
//    Lock readLock = lock.readLock();
//    RpcHandler handler;
//    try {
//      readLock.lock();
//      handler = cache.get(type);
//    } finally {
//      readLock.unlock();
//    }
//    if (handler != null) {
//      return handler;
//    }
//    Lock writeLock = lock.writeLock();
//    try {
//      writeLock.lock();
//      handler = cache.get(type);
//      if (handler == null) {
//        handler = newHandler(type, vertx, config);
//        cache.putIfAbsent(type, handler);
//      }
//    } finally {
//      writeLock.unlock();
//    }
//    return handler;
//  }
//
//  static RpcHandler newHandler(String type, Vertx vertx, JsonObject config) {
//    RpcHandler handler;
//    Iterator<RpcHandlerFactory> iterator = factories.stream().filter(f -> type.equalsIgnoreCase
// (f.type()))
//        .iterator();
//    if (iterator.hasNext()) {
//      handler = iterator.next().create(vertx, config);
//    }  else {
//      handler = failedRpcHandler;
//    }
//    return handler;
//  }

  String type();

  Future<RpcResponse> handle(RpcRequest rpcRequest);
}
