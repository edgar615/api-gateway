package com.edgar.direwolves.core.loabalance;

/**
 * Created by Edgar on 2017/7/28.
 *
 * @author Edgar  Date 2017/7/28
 */
public class Httploadbanlance {
//  IRule rule = new AvailabilityFilteringRule();
//  ServerList<DiscoveryEnabledServer> list = new DiscoveryEnabledNIWSServerList("MyVIP:7001");
//  ServerListFilter<DiscoveryEnabledServer> filter = new ZoneAffinityServerListFilter<DiscoveryEnabledServer>();
//  ZoneAwareLoadBalancer<DiscoveryEnabledServer> lb = LoadBalancerBuilder.<DiscoveryEnabledServer>newBuilder()
//          .withDynamicServerList(list)
//          .withRule(rule)
//          .withServerListFilter(filter)
//          .buildDynamicServerListLoadBalancer();
//  DiscoveryEnabledServer server = lb.chooseServer();


//  HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup("movieServiceClient",
//                                                                       ClientOptions.create()
//                                                                               .withMaxAutoRetriesNextServer(3)
//                                                                               .withConfigurationBasedServerList("localhost:8080,localhost:8088"));
//  HttpRequestTemplate<ByteBuf> recommendationsByUserIdTemplate = httpResourceGroup.newTemplateBuilder("recommendationsByUserId", ByteBuf.class)
//          .withMethod("GET")
//          .withUriTemplate("/users/{userId}/recommendations")
//          .withFallbackProvider(new RecommendationServiceFallbackHandler())
//          .withResponseValidator(new RecommendationServiceResponseValidator())
//          .build();
//  Observable<ByteBuf> result = recommendationsByUserIdTemplate.requestBuilder()
//          .withRequestProperty("userId", “user1")
//                  .build()
//                  .observe();
}
