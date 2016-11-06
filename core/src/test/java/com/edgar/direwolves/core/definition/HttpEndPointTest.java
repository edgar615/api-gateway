package com.edgar.direwolves.core.definition;

import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2016/9/8.
 *
 * @author Edgar  Date 2016/9/8
 */
public class HttpEndPointTest {

  @Test
  public void testBuild() {
    HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "/devices", "device");

    Assert.assertEquals("/devices", httpEndpoint.path());
  }

}
