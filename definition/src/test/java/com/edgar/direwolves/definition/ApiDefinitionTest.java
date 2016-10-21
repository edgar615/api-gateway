//package com.edgar.direwolves.definition;
//
//import com.google.common.collect.Lists;
//
//import io.vertx.core.http.HttpMethod;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * Created by Edgar on 2016/9/8.
// *
// * @author Edgar  Date 2016/9/8
// */
//public class ApiDefinitionTest {
//
//  @Test
//  public void testCreate() {
//
//    HttpEndpoint httpEndpoint =
//            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
//
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//    Assert.assertEquals("/devices", apiDefinition.path());
//    Assert.assertEquals("default", apiDefinition.scope());
//  }
//
//  @Test
//  public void testEndpointsShouldNotEmpty() {
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList());
//    try {
//      ApiDefinition.create(option);
//      Assert.fail();
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//      Assert.assertTrue(e instanceof IllegalArgumentException);
//    }
//  }
//
//  @Test
//  public void testEndpointsShouldImmutable() {
//    HttpEndpoint httpEndpoint =
//            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    httpEndpoint =
//            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
//    try {
//      apiDefinition.endpoints().add(httpEndpoint);
//      Assert.fail();
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//      Assert.assertTrue(e instanceof UnsupportedOperationException);
//    }
//  }
//
//  @Test
//  public void testUrlArgsShouldImmutable() {
//    HttpEndpoint httpEndpoint =
//            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setUrlArgs(Lists.newArrayList(new ParameterImpl("username", null)))
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//    try {
//      apiDefinition.urlArgs().add(new ParameterImpl("password", null));
//      Assert.fail();
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//      Assert.assertTrue(e instanceof UnsupportedOperationException);
//    }
//  }
//
//    @Test
//    public void testBodyArgsShouldImmutable() {
//      HttpEndpoint httpEndpoint =
//              Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
//      ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//              .setMethod(HttpMethod.POST)
//              .setPath("devices/")
//              .setBodyArgs(Lists.newArrayList(new ParameterImpl("username", null)))
//              .setEndpoints(Lists.newArrayList(httpEndpoint));
//      ApiDefinition apiDefinition = ApiDefinition.create(option);
//      try {
//        apiDefinition.bodyArgs().add(new ParameterImpl("password", null));
//        Assert.fail();
//      } catch (Exception e) {
//        System.out.println(e.getMessage());
//        Assert.assertTrue(e instanceof UnsupportedOperationException);
//      }
//    }
//
//
//  @Test
//  public void testFilter() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    apiDefinition.addFilter("jwt");
//    apiDefinition.addFilter("app_key");
//    apiDefinition.addFilter("app_key");
//    Assert.assertEquals(2, apiDefinition.filters().size());
//
//    apiDefinition.removeFilter("app_key");
//
//    Assert.assertEquals(1, apiDefinition.filters().size());
//
//  }
//
//  @Test
//  public void testBlackList() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    apiDefinition.addBlacklist("192.168.1.100");
//    Assert.assertEquals(1, apiDefinition.blacklist().size());
//    apiDefinition.addBlacklist("192.168.1.100");
//    apiDefinition.addBlacklist("192.168.1.101");
//
//    Assert.assertEquals(2, apiDefinition.blacklist().size());
//
//    apiDefinition.removeBlacklist("192.168.1.101");
//
//    Assert.assertEquals(1, apiDefinition.blacklist().size());
//
//  }
//
//  @Test
//  public void testWhiteList() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    apiDefinition.addWhitelist("192.168.1.100");
//    Assert.assertEquals(1, apiDefinition.whitelist().size());
//    apiDefinition.addWhitelist("192.168.1.100");
//    apiDefinition.addWhitelist("192.168.1.101");
//
//    Assert.assertEquals(2, apiDefinition.whitelist().size());
//
//    apiDefinition.removeWhitelist("192.168.1.101");
//
//    Assert.assertEquals(1, apiDefinition.whitelist().size());
//
//  }
//
//  @Test
//  public void testUniqueRateLimit() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    apiDefinition.addRateLimit(RateLimitPlugin.create("token", "second", 100));
//    apiDefinition.addRateLimit(RateLimitPlugin.create("token", "day", 100));
//    apiDefinition.addRateLimit(RateLimitPlugin.create("user", "second", 100));
//    Assert.assertEquals(3, apiDefinition.rateLimits().size());
//
//    apiDefinition.addRateLimit(RateLimitPlugin.create("token", "second", 1000));
//    apiDefinition.addRateLimit(RateLimitPlugin.create("token", "day", 1000));
//    apiDefinition.addRateLimit(RateLimitPlugin.create("user", "second", 1000));
//    Assert.assertEquals(3, apiDefinition.rateLimits().size());
//
//    List<RateLimitPlugin> filterDefintions = apiDefinition.rateLimits().stream()
//            .filter(d -> "token".equalsIgnoreCase(d.limitBy())
//                         && "day".equalsIgnoreCase(d.type()))
//            .collect(Collectors.toList());
//    RateLimitPlugin rateLimit = filterDefintions.get(0);
//    Assert.assertEquals(1000, rateLimit.limit());
//  }
//
//  @Test
//  public void testMatcher() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/123"));
//
//
//    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
//                                                    "device");
//    option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/([\\d+]+)")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    apiDefinition = ApiDefinition.create(option);
//
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
//    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));
//
//    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices",
//                                       "device");
//    option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/([\\w+]+)")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    apiDefinition = ApiDefinition.create(option);
//
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.POST, "/devices"));
//    Assert.assertFalse(apiDefinition.match(HttpMethod.GET, "/devices/"));
//    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/abc"));
//    Assert.assertTrue(apiDefinition.match(HttpMethod.GET, "/devices/123"));
//  }
//
//  @Test
//  public void testAddRequestTransformer() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    RequestTransformer transformer = RequestTransformer.create("device");
//    transformer.addBody("b1", "v1");
//    transformer.addHeader("h1", "v1");
//    transformer.addParam("p1", "v1");
//    transformer.addBody("b2", "v2");
//    transformer.addHeader("h2", "v2");
//    transformer.addParam("p2", "v2");
//
//    transformer.removeBody("b3");
//    transformer.removeHeader("h3");
//    transformer.removeParam("p3");
//
//    transformer.replaceBody("b4", "v4");
//    transformer.replaceHeader("h4", "v4");
//    transformer.replaceParam("p4", "v4");
//
//    apiDefinition.addRequestTransformer(transformer);
//
//    Assert.assertEquals(1, apiDefinition.requestTransformer().size());
//    Assert.assertEquals(2, apiDefinition.requestTransformer().get(0).bodyAdded().size());
//    Assert.assertEquals(2, apiDefinition.requestTransformer().get(0).headerAdded().size());
//    Assert.assertEquals(2, apiDefinition.requestTransformer().get(0).paramAdded().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).bodyRemoved().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).headerRemoved().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).paramRemoved().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).bodyReplaced().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).headerReplaced().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).paramReplaced().size());
//
//    transformer = RequestTransformer.create("device");
//    transformer.addBody("b1", "v1");
//
//    apiDefinition.addRequestTransformer(transformer);
//
//    Assert.assertEquals(1, apiDefinition.requestTransformer().size());
//    Assert.assertEquals(1, apiDefinition.requestTransformer().get(0).bodyAdded().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).headerAdded().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).paramAdded().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).bodyRemoved().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).headerRemoved().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).paramRemoved().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).bodyReplaced().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).headerReplaced().size());
//    Assert.assertEquals(0, apiDefinition.requestTransformer().get(0).paramReplaced().size());
//
//    transformer = RequestTransformer.create("device2");
//    transformer.addBody("b1", "v1");
//
//    apiDefinition.addRequestTransformer(transformer);
//
//    Assert.assertEquals(2, apiDefinition.requestTransformer().size());
//
//  }
//
//  @Test
//  public void testRemoveRequestTransfomer() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    RequestTransformer transformer = RequestTransformer.create("device");
//    transformer.addBody("b1", "v1");
//    transformer.addHeader("h1", "v1");
//    transformer.addParam("p1", "v1");
//    transformer.addBody("b2", "v2");
//    transformer.addHeader("h2", "v2");
//    transformer.addParam("p2", "v2");
//
//    transformer.removeBody("b3");
//    transformer.removeHeader("h3");
//    transformer.removeParam("p3");
//
//    transformer.replaceBody("b4", "v4");
//    transformer.replaceHeader("h4", "v4");
//    transformer.replaceParam("p4", "v4");
//    apiDefinition.addRequestTransformer(transformer);
//
//    Assert.assertEquals(1, apiDefinition.requestTransformer().size());
//    apiDefinition.removeRequestTransformer("device");
//    Assert.assertEquals(0, apiDefinition.requestTransformer().size());
//
//  }
//
//  @Test
//  public void testAddResponseTransformer() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    ResponseTransformer transformer = ResponseTransformer.create("device");
//    transformer.addBody("b1", "v1");
//    transformer.addHeader("h1", "v1");
//    transformer.addBody("b2", "v2");
//    transformer.addHeader("h2", "v2");
//    transformer.removeBody("b3");
//    transformer.removeHeader("h3");
//    transformer.replaceBody("b4", "v4");
//    transformer.replaceHeader("h4", "v4");
//
//    apiDefinition.addResponseTransformer(transformer);
//
//    Assert.assertEquals(1, apiDefinition.responseTransformer().size());
//    Assert.assertEquals(2, apiDefinition.responseTransformer().get(0).bodyAdded().size());
//    Assert.assertEquals(2, apiDefinition.responseTransformer().get(0).headerAdded().size());
//    Assert.assertEquals(1, apiDefinition.responseTransformer().get(0).bodyRemoved().size());
//    Assert.assertEquals(1, apiDefinition.responseTransformer().get(0).headerRemoved().size());
//    Assert.assertEquals(1, apiDefinition.responseTransformer().get(0).bodyReplaced().size());
//    Assert.assertEquals(1, apiDefinition.responseTransformer().get(0).headerReplaced().size());
//
//    transformer = ResponseTransformer.create("device");
//    transformer.addBody("b1", "v1");
//
//    apiDefinition.addResponseTransformer(transformer);
//
//    Assert.assertEquals(1, apiDefinition.responseTransformer().size());
//    Assert.assertEquals(1, apiDefinition.responseTransformer().get(0).bodyAdded().size());
//    Assert.assertEquals(0, apiDefinition.responseTransformer().get(0).headerAdded().size());
//    Assert.assertEquals(0, apiDefinition.responseTransformer().get(0).bodyRemoved().size());
//    Assert.assertEquals(0, apiDefinition.responseTransformer().get(0).headerRemoved().size());
//    Assert.assertEquals(0, apiDefinition.responseTransformer().get(0).bodyReplaced().size());
//    Assert.assertEquals(0, apiDefinition.responseTransformer().get(0).headerReplaced().size());
//
//    transformer = ResponseTransformer.create("device2");
//    transformer.addBody("b1", "v1");
//
//    apiDefinition.addResponseTransformer(transformer);
//
//    Assert.assertEquals(2, apiDefinition.responseTransformer().size());
//
//  }
//
//  @Test
//  public void testRemoveResponseTransfomer() {
//    HttpEndpoint httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "devices/",
//                                                    "device");
//    ApiDefinitionOption option = new ApiDefinitionOption().setName("get_device")
//            .setMethod(HttpMethod.GET)
//            .setPath("devices/")
//            .setEndpoints(Lists.newArrayList(httpEndpoint));
//    ApiDefinition apiDefinition = ApiDefinition.create(option);
//
//    ResponseTransformer transformer = ResponseTransformer.create("device");
//    transformer.addBody("b1", "v1");
//    transformer.addHeader("h1", "v1");
//    transformer.addBody("b2", "v2");
//    transformer.addHeader("h2", "v2");
//    transformer.removeBody("b3");
//    transformer.removeHeader("h3");
//    transformer.replaceBody("b4", "v4");
//    transformer.replaceHeader("h4", "v4");
//
//    apiDefinition.addResponseTransformer(transformer);
//
//    Assert.assertEquals(1, apiDefinition.responseTransformer().size());
//    apiDefinition.removeResponseTransformer("device");
//    Assert.assertEquals(0, apiDefinition.responseTransformer().size());
//
//  }
//}
