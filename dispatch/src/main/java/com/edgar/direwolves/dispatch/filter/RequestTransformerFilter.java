//package com.edgar.direwolves.dispatch.filter;
//
//import com.edgar.direwolves.core.spi.ApiContext;
//import com.edgar.direwolves.plugin.transformer.RequestTransformer;
//import com.edgar.direwolves.plugin.transformer.RequestTransformerPlugin;
//import io.vertx.core.Future;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//
///**
// * request_transfomer.
// * <p>
// * </pre>
// * <p>
// * Created by edgar on 16-9-20.
// */
//public class RequestTransformerFilter implements Filter {
//
//  private static final String NAME = "request_transfomer";
//
//  private Vertx vertx;
//
//  @Override
//  public String name() {
//    return NAME;
//  }
//
//  @Override
//  public String type() {
//    return PRE;
//  }
//
//  @Override
//  public void config(Vertx vertx, JsonObject config) {
//    this.vertx = vertx;
//  }
//
//  @Override
//  public boolean shouldFilter(ApiContext apiContext) {
//    if (apiContext.apiDefinition() == null) {
//      return false;
//    }
//    return apiContext.apiDefinition().plugin(RequestTransformerPlugin.NAME) != null;
//  }
//
//  @Override
//  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
//
//    for (int i = 0; i < apiContext.request().size(); i++) {
//      JsonObject request = apiContext.request().getJsonObject(i);
//      transformer(apiContext, request);
//    }
//    completeFuture.complete(apiContext);
//  }
//
//
//  private void transformer(ApiContext apiContext, JsonObject request) {
//    String name = request.getString("name");
//    RequestTransformerPlugin plugin =
//            (RequestTransformerPlugin) apiContext.apiDefinition()
//                    .plugin(RequestTransformerPlugin.NAME);
//
//    RequestTransformer transformer = plugin.transformer(name);
//    if (transformer != null) {
//      tranformerParams(request.getJsonObject("params"), transformer);
//      tranformerHeaders(request.getJsonObject("headers"), transformer);
//      if (request.containsKey("body")) {
//        tranformerBody(request.getJsonObject("body"), transformer);
//      }
//    }
//  }
//
//  private void tranformerParams(JsonObject params,
//                                RequestTransformer transformer) {
//    transformer.paramRemoved().forEach(p -> params.remove(p));
//    transformer.paramReplaced().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
//    transformer.paramAdded().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
//  }
//
//  private void tranformerHeaders(JsonObject headers,
//                                 RequestTransformer transformer) {
//    transformer.headerRemoved().forEach(h -> headers.remove(h));
//    transformer.headerReplaced().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
//    transformer.headerAdded().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
//  }
//
//  private void tranformerBody(final JsonObject body,
//                              RequestTransformer transformer) {
//    if (body != null) {
//      transformer.bodyRemoved().forEach(b -> body.remove(b));
//    }
//    //replace
//    if (body != null) {
//      transformer.bodyReplaced().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
//    }
//
//    //add
//    if (body != null) {
//      transformer.bodyAdded().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
//    }
//  }
//
//}