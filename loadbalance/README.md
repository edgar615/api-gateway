以前的service-discovery并不能完全满足需求，计划使用loadbalance替换

loadbalance通过一系列的filter(名称、断路器、版本)来从节点列表中选择一个节点——选择策略也可以自定义

HTTP请求依赖于loadbalance，HTTP请求只定义到服务名，而不指定具体下游服务的IP和断开，
直到发起请求之前才通过loadbalance选取合适的服务节点，同时要使用断路器和降级策略。


IRule

BestAvailableRule
选择一个最小的并发请求的server

逐个考察Server，如果Server被tripped了，则忽略，在选择其中ActiveRequestsCount最小的server

AvailabilityFilteringRule
过滤掉那些因为一直连接失败的被标记为circuit tripped的后端server，并过滤掉那些高并发的的后端server（active connections 超过配置的阈值）
使用一个AvailabilityPredicate来包含过滤server的逻辑，其实就就是检查status里记录的各个server的运行状态

WeightedResponseTimeRule
根据相应时间分配一个weight，相应时间越长，weight越小，被选中的可能性越低。
一个后台线程定期的从status里面读取评价响应时间，为每个server计算一个weight。
Weight的计算也比较简单responsetime 减去每个server自己平均的responsetime是server的权重。
当刚开始运行，没有形成statas时，使用roubine策略选择server。

RetryRule
对选定的负载均衡策略机上重试机制。
在一个配置时间段内当选择server不成功，则一直尝试使用subRule的方式选择一个可用的server

RoundRobinRule
roundRobin方式轮询选择server
轮询index，选择index对应位置的server

RandomRule
随机选择一个server
 在index上随机，选择index对应位置的server

ZoneAvoidanceRule
复合判断server所在区域的性能和server的可用性选择server

使用ZoneAvoidancePredicate和AvailabilityPredicate来判断是否选择某个server，
前一个判断判定一个zone的运行性能是否可用，剔除不可用的zone（的所有server），
AvailabilityPredicate用于过滤掉连接数过多的Server。




1. com.netflix.loadbalancer.BestAvailableRule

功能：选择一个最小的并发请求的server

主要代码：逐个考察Server，如果Server被tripped了，则忽略，在选择其中ActiveRequestsCount最小的server

for (Serverserver: serverList) {
ServerStatsserverStats = loadBalancerStats.getSingleServerStat(server);
if (!serverStats.isCircuitBreakerTripped(currentTime)) {
int concurrentConnections = serverStats.getActiveRequestsCount(currentTime);
if (concurrentConnections < minimalConcurrentConnections) {
minimalConcurrentConnections = concurrentConnections;
chosen = server;
}
}



2 com.netflix.loadbalancer.AvailabilityFilteringRule

功能：过滤掉那些因为一直连接失败的被标记为circuit tripped的后端server，并过滤掉那些高并发的的后端server（active connections 超过配置的阈值）

主要代码：使用一个AvailabilityPredicate来包含过滤server的逻辑，其实就就是检查status里记录的各个server的运行状态，过滤掉那些高并发的的后端server（active connections 超过配置的阈值）

boolean com.netflix.loadbalancer.AvailabilityPredicate.shouldSkipServer(ServerStatsstats)
{
if ((CIRCUIT_BREAKER_FILTERING.get() && stats.isCircuitBreakerTripped())
|| stats.getActiveRequestsCount() >= activeConnectionsLimit.get()) {
return true;
}
return false;
}

3 com.netflix.loadbalancer.WeightedResponseTimeRule

功能：根据相应时间分配一个weight，相应时间越长，weight越小，被选中的可能性越低。 ”

主要代码：一个后台线程定期的从status里面读取评价响应时间，为每个server计算一个weight。Weight的计算也比较简单responsetime 减去每个server自己平均的responsetime是server的权重。当刚开始运行，没有形成statas时，使用roubine策略选择server。

class DynamicServerWeightTask extends TimerTask {
public void run() {
ServerWeightserverWeight = new ServerWeight();
serverWeight.maintainWeights();
}
}

maintainWeights(){
List<Double> finalWeights = new ArrayList<Double>();
for (Serverserver : nlb.getAllServers()) {
ServerStatsss = stats.getSingleServerStat(server);
double weight = totalResponseTime – ss.getResponseTimeAvg();
weightSoFar += weight;
finalWeights.add(weightSoFar);
}
setWeights(finalWeights);}

Serverchoose(ILoadBalancerlb, Object key)
{
double randomWeight = random.nextDouble() * maxTotalWeight;
// pick the server index based on the randomIndex
int n = 0;
for (Double d : currentWeights) {
if (d >= randomWeight) {
serverIndex = n;
break;
} else {
n++;
}
}

server = allList.get(serverIndex);}

4 com.netflix.loadbalancer.RetryRule

功能：对选定的负载均衡策略机上重试机制。

主要代码：在一个配置时间段内当选择server不成功，则一直尝试使用subRule的方式选择一个可用的server

answer = subRule.choose(key);
if (((answer == null) || (!answer.isAlive()))
&& (System.currentTimeMillis() < deadline)) {
InterruptTasktask = new InterruptTask(deadline - System.currentTimeMillis());
while (!Thread.interrupted()) {
answer = subRule.choose(key);
if (((answer == null) || (!answer.isAlive()))
&& (System.currentTimeMillis() < deadline)) {
/* pause and retry hoping it’s transient */
Thread.yield();
} else {
break;
}
}
task.cancel();

5 com.netflix.loadbalancer.RoundRobinRule

功能：roundRobin方式轮询选择server

主要代码：轮询index，选择index对应位置的server

List<Server> allServers = lb.getAllServers();
int upCount = reachableServers.size();
int serverCount = allServers.size();
int nextServerIndex = incrementAndGetModulo(serverCount);
server = allServers.get(nextServerIndex);

6 com.netflix.loadbalancer.RandomRule

功能：随机选择一个server

主要代码：在index上随机，选择index对应位置的server

List<Server> upList = lb.getReachableServers();
List<Server> allList = lb.getAllServers();
int serverCount = allList.size();
int index = rand.nextInt(serverCount);
server = upList.get(index);

7 com.netflix.loadbalancer.ZoneAvoidanceRule

功能：复合判断server所在区域的性能和server的可用性选择server

主要代码：使用ZoneAvoidancePredicate和AvailabilityPredicate来判断是否选择某个server，前一个，以一个区域为单位考察可用性，对于不可用的区域整个丢弃，从剩下区域中选可用的server。判断出最差的区域，排除掉最差区域。在剩下的区域中，将按照服务器实例数的概率抽样法选择，从而判断判定一个zone的运行性能是否可用，剔除不可用的zone（的所有server），AvailabilityPredicate用于过滤掉连接数过多的Server。

public com.netflix.loadbalancer.PredicateBasedRule.Serverchoose(Object key) {
ILoadBalancerlb = getLoadBalancer();
Optional<Server> server = getPredicate().chooseRoundRobinAfterFiltering(lb.getAllServers(), key);
if (server.isPresent()) {
return server.get();
}
}

参照现有的若干中rule的实现风格，根据我们自己需要也可以开发出自定义的负载均衡策略

