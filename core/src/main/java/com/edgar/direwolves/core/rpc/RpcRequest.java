package com.edgar.direwolves.core.rpc;

public interface RpcRequest {

  String id();

  String name();

  String type();

  RpcRequest copy();

}