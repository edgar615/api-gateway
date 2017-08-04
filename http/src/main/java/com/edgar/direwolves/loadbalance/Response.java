package com.edgar.direwolves.loadbalance;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

/**
 * Created by Edgar on 2017/8/1.
 *
 * @author Edgar  Date 2017/8/1
 */
@Deprecated
public class Response {

  private int statusCode;

  private MultiMap header;

  private Buffer body;

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public MultiMap getHeader() {
    return header;
  }

  public void setHeader(MultiMap header) {
    this.header = header;
  }

  public Buffer getBody() {
    return body;
  }

  public void setBody(Buffer body) {
    this.body = body;
  }
}
