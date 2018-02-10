package com.github.edgar615.direvolves.plugin.gray;

/**
 * Created by Edgar on 2018/2/10.
 *
 * @author Edgar  Date 2018/2/10
 */
public class ServiceSplitter {

  /**
   * 给下游服务增加的后缀，如果下游服务名为user，经过分流后会请求user-<serviceSuffix>
   */
  private String serviceSuffix;
}
