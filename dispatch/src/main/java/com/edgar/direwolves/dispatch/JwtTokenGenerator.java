//package com.edgar.direwolves.dispatch;
//
//import com.edgar.direwolves.core.spi.Configurable;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//
///**
// * Created by edgar on 16-9-20.
// */
//public class JwtTokenGenerator implements TokenGenerator, Configurable {
//
//  private Vertx vertx;
//
//  private JsonObject config = new JsonObject()
//      .put("path", "keystore.jceks")
//      .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
//      .put("password", "secret")
//      .put("algorithm", "HS512")
//      .put("expiresInSeconds", 1800);
//
//  /**
//   * 配置JwtTokenGenerator.
//   * <pre>
//   *     - keystore.path string 证书文件路径 默认值keystore.jceks
//   *     - keystore.type string 证书类型，可选值 jceks, jks,默认值jceks
//   *     - keystore.password string 证书密钥，默认值secret
//   *     - jwt.alg string jwt的加密算法,默认值HS512
//   *     - jwt.audience string token的客户aud
//   *     - jwt.issuer string token的发行者iss
//   *     - jwt.subject string token的主题sub
//   *     - jwt.expires int 过期时间exp，单位秒，默认值1800
//   * </pre>
//   *
//   * @param vertx  Vertx
//   * @param config 配置
//   */
//  @Override
//  public void config(Vertx vertx, JsonObject config) {
//    this.vertx = vertx;
//    if (config.containsKey("keystore.path")) {
//      this.config.put("path", config.getString("keystore.path"));
//    }
//    if (config.containsKey("keystore.type")) {
//      this.config.put("type", config.getString("keystore.type"));
//    }
//    if (config.containsKey("keystore.password")) {
//      this.config.put("password", config.getString("keystore.password"));
//    }
//    if (config.containsKey("jwt.alg")) {
//      this.config.put("algorithm", config.getString("jwt.alg"));
//    }
//    if (config.containsKey("jwt.audience")) {
//      this.config.put("audience", config.getString("jwt.audience"));
//    }
//    if (config.containsKey("jwt.issuer")) {
//      this.config.put("issuer", config.getString("jwt.issuer"));
//    }
//    if (config.containsKey("jwt.subject")) {
//      this.config.put("subject", config.getString("jwt.subject"));
//    }
//    if (config.containsKey("jwt.expires")) {
//      this.config.put("expiresInSeconds", config.getInteger("jwt.expires"));
//    }
//  }
//
//  @Override
//  public String createToken(JsonObject claims) {
//    JsonObject jwtConfig = new JsonObject().put("keyStore", config);
//    JWTAuth provider = JWTAuth.create(vertx, jwtConfig);
//    return provider.generateToken(claims, new JWTOptions(config));
//  }
//}
