//package com.edgar.direwolves.filter;
//
//import com.edgar.direwolves.plugin.transformer.RequestTransformer;
//import com.edgar.direwolves.dispatch.ApiContext;
//import io.vertx.core.Future;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//
//import java.util.List;
//import java.util.stream.Collectors;
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
//    List<String> filters = apiContext.apiDefinition().filters();
//    return filters.contains(NAME);
//  }
//
//  @Override
//  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
//
//    for (int i = 0; i < apiContext.request().size(); i++) {
//      JsonObject request = apiContext.request().getJsonObject(i);
//      transformerParams(apiContext, request);
//    }
//    completeFuture.complete(apiContext);
//  }
//
//
//  private void transformerParams(ApiContext apiContext, JsonObject request) {
//    String name = request.getString("name");
//    JsonObject params = request.getJsonObject("params");
//    JsonObject headers = request.getJsonObject("headers");
//    JsonObject body = null;
//    if (request.containsKey("body")) {
//      body = request.getJsonObject("body");
//    }
//    List<RequestTransformer> transformers = apiContext.apiDefinition().requestTransformer()
//            .stream()
//            .filter(t -> t.name().equalsIgnoreCase(name))
//            .collect(Collectors.toList());
//    if (transformers.size() > 0) {
//      RequestTransformer transformer = transformers.get(0);
//      tranformer(params, headers, body, transformer);
//    }
//  }
//
//  private void tranformer(JsonObject params, JsonObject headers, final JsonObject body,
//                          RequestTransformer transformer) {//delete
//    transformer.paramRemoved().forEach(p -> params.remove(p));
//    transformer.headerRemoved().forEach(h -> headers.remove(h));
//    if (body != null) {
//      transformer.bodyRemoved().forEach(b -> body.remove(b));
//    }
//    //replace
//    transformer.paramReplaced().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
//    transformer.headerReplaced().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
//    if (body != null) {
//      transformer.bodyReplaced().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
//    }
//
//    //add
//    transformer.paramAdded().forEach(entry -> params.put(entry.getKey(), entry.getValue()));
//    transformer.headerAdded().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
//    if (body != null) {
//      transformer.bodyAdded().forEach(entry -> body.put(entry.getKey(), entry.getValue()));
//    }
//  }
//
//}
