package com.edgar.direwolves.core.utils;

/**
 * Created by Edgar on 2017/7/7.
 *
 * @author Edgar  Date 2017/7/7
 */
public enum LogType {

  /**
   * Server Received 服务端收到请求.
   */
  SR,

  /**
   * Server Send，服务端处理完请求，向客户端返回.
   */
  SS,

  /**
   * Client Send，客户端向服务端（包括下游服务，DB等等）发起请求.
   */
  CS,

  /**
   * Client Received 客户端收到服务端的响应.
   */
  CR,

  /**
   * 本地产生的一些业务日志，比如：请求成功，请求失败.
   */
  LOG,

  /**
   * Server Event send，向Eventbus发送事件，主要是对send请求的回应.
   */
  SES,

  /**
   * Server Event Received，从eventbus收到事件.
   */
  SER,
  /**
   * Client Event send，向Eventbus发送事件.
   */
  CES,

  /**
   * Client Event Received，从eventbus中接受事件，主要是对send请求的回应
   */
  CER;
}
