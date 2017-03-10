package com.edgar.direwolves.core.definition;

import io.vertx.core.http.HttpMethod;

/**
 * 定义远程服务调用的格式.
 *
 * @author Edgar  Date 2016/9/12
 */
public interface Endpoint {

  /**
   * @return endpoint的名称
   */
  String name();

  /**
   * @return endpoint的类型
   */
  String type();

  /**
   * 创建HTTP类型的Endpoint
   *
   * @param name    名称
   * @param method  请求方法 GET | POST | DELETE | PUT
   * @param path    API路径
   * @param service 服务名，用于服务发现
   * @return HttpEndpoint
   */
  static HttpEndpoint http(String name, HttpMethod method, String path, String service) {
    return new HttpEndpointImpl(name, method, path, service);
  }

  /**
   * 创建Req-Resp类型的Endpoint
   *
   * @param name    名称
   * @param address 事件地址
   * @param action  操作，方法，如果不为null，会在EventBus的消息头中增加 action : ${action}的头，用来匹配RPC调用.
   * @return
   */
  static EventbusEndpoint reqResp(String name, String address, String action) {
    return new EventbusEndpointImpl(name, address, EventbusEndpoint.REQ_RESP, action);
  }

  /**
   * 创建广播类型的Endpoint
   *
   * @param name    名称
   * @param address 事件地址
   * @return
   */
  static EventbusEndpoint publish(String name, String address) {
    return new EventbusEndpointImpl(name, address, EventbusEndpoint.PUB_SUB, null);
  }

  /**
   * 创建点对点类型的Endpoint
   *
   * @param name    名称
   * @param address 事件地址
   * @return
   */
  static EventbusEndpoint pointToPoint(String name, String address) {
    return new EventbusEndpointImpl(name, address, EventbusEndpoint.POINT_POINT, null);
  }

}
