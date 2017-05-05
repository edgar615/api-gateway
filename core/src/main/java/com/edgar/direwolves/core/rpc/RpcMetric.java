package com.edgar.direwolves.core.rpc;

/**
 * Created by Edgar on 2017/5/4.
 *
 * @author Edgar  Date 2017/5/4
 */
public interface RpcMetric {
  void request(String server);

  void response(String server, int result, long duration);
}
