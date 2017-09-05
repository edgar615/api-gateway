package com.edgar.direwolves.redis;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisTransaction;
import io.vertx.redis.op.*;

import java.util.List;
import java.util.Map;

/**
 * Wraps a client with the {@link ClientHolder} in order to keep track of the references.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClientWrapper implements RedisClient {

  private final ClientHolder holder;

  private final RedisClient client;

  public ClientWrapper(ClientHolder holder) {
    this.holder = holder;
    this.client = holder.client();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> whenDone) {
    holder.close(whenDone);
  }

  @Override
  public RedisClient append(String key, String value, Handler<AsyncResult<Long>> handler) {
    client.append(key, value, handler);
    return this;
  }

  @Override
  public RedisClient auth(String password, Handler<AsyncResult<String>> handler) {
    client.auth(password, handler);
    return this;
  }

  @Override
  public RedisClient bgrewriteaof(Handler<AsyncResult<String>> handler) {
    client.bgrewriteaof(handler);
    return this;
  }

  @Override
  public RedisClient bgsave(Handler<AsyncResult<String>> handler) {
    client.bgsave(handler);
    return this;
  }

  @Override
  public RedisClient bitcount(String key, Handler<AsyncResult<Long>> handler) {
    client.bitcount(key, handler);
    return this;
  }

  @Override
  public RedisClient bitcountRange(String key, long start, long end,
                                   Handler<AsyncResult<Long>> handler) {
    client.bitcountRange(key, start, end, handler);
    return this;
  }

  @Override
  public RedisClient bitop(BitOperation operation, String destkey, List<String> keys,
                           Handler<AsyncResult<Long>> handler) {
    client.bitop(operation, destkey, keys, handler);
    return this;
  }

  @Override
  public RedisClient bitpos(String key, int bit, Handler<AsyncResult<Long>> handler) {
    client.bitpos(key, bit, handler);
    return this;
  }

  @Override
  public RedisClient bitposFrom(String key, int bit, int start,
                                Handler<AsyncResult<Long>> handler) {
    client.bitposFrom(key, bit, start, handler);
    return this;
  }

  @Override
  public RedisClient bitposRange(String key, int bit, int start, int stop,
                                 Handler<AsyncResult<Long>> handler) {
    client.bitposRange(key, bit, start, stop, handler);
    return this;
  }

  @Override
  public RedisClient blpop(String key, int seconds, Handler<AsyncResult<JsonArray>> handler) {
    client.blpop(key, seconds, handler);
    return this;
  }

  @Override
  public RedisClient blpopMany(List<String> keys, int seconds,
                               Handler<AsyncResult<JsonArray>> handler) {
    client.blpopMany(keys, seconds, handler);
    return this;
  }

  @Override
  public RedisClient brpop(String key, int seconds, Handler<AsyncResult<JsonArray>> handler) {
    client.brpop(key, seconds, handler);
    return this;
  }

  @Override
  public RedisClient brpopMany(List<String> keys, int seconds,
                               Handler<AsyncResult<JsonArray>> handler) {
    client.brpopMany(keys, seconds, handler);
    return this;
  }

  @Override
  public RedisClient brpoplpush(String key, String destkey, int seconds,
                                Handler<AsyncResult<String>> handler) {
    client.brpoplpush(key, destkey, seconds, handler);
    return this;
  }

  @Override
  public RedisClient clientKill(KillFilter filter, Handler<AsyncResult<Long>> handler) {
    client.clientKill(filter, handler);
    return this;
  }

  @Override
  public RedisClient clientList(Handler<AsyncResult<String>> handler) {
    client.clientList(handler);
    return this;
  }

  @Override
  public RedisClient clientGetname(Handler<AsyncResult<String>> handler) {
    client.clientGetname(handler);
    return this;
  }

  @Override
  public RedisClient clientPause(long millis, Handler<AsyncResult<String>> handler) {
    client.clientPause(millis, handler);
    return this;
  }

  @Override
  public RedisClient clientSetname(String name, Handler<AsyncResult<String>> handler) {
    client.clientSetname(name, handler);
    return this;
  }

  @Override
  public RedisClient clusterAddslots(List<Long> slots, Handler<AsyncResult<Void>> handler) {
    client.clusterAddslots(slots, handler);
    return this;
  }

  @Override
  public RedisClient clusterCountFailureReports(String nodeId, Handler<AsyncResult<Long>> handler) {
    client.clusterCountFailureReports(nodeId, handler);
    return this;
  }

  @Override
  public RedisClient clusterCountkeysinslot(long slot, Handler<AsyncResult<Long>> handler) {
    client.clusterCountkeysinslot(slot, handler);
    return this;
  }

  @Override
  public RedisClient clusterDelslots(long slot, Handler<AsyncResult<Void>> handler) {
    client.clusterDelslots(slot, handler);
    return this;
  }

  @Override
  public RedisClient clusterDelslotsMany(List<Long> slots, Handler<AsyncResult<Void>> handler) {
    client.clusterDelslotsMany(slots, handler);
    return this;
  }

  @Override
  public RedisClient clusterFailover(Handler<AsyncResult<Void>> handler) {
    client.clusterFailover(handler);
    return this;
  }

  @Override
  public RedisClient clusterFailOverWithOptions(FailoverOptions options,
                                                Handler<AsyncResult<Void>> handler) {
    client.clusterFailOverWithOptions(options, handler);
    return this;
  }

  @Override
  public RedisClient clusterForget(String nodeId, Handler<AsyncResult<Void>> handler) {
    client. clusterForget(nodeId, handler);
    return this;
  }

  @Override
  public RedisClient clusterGetkeysinslot(long slot, long count,
                                          Handler<AsyncResult<JsonArray>> handler) {
    client. clusterGetkeysinslot(slot, count, handler);
    return this;
  }

  @Override
  public RedisClient clusterInfo(Handler<AsyncResult<JsonArray>> handler) {
    client.clusterInfo(handler);
    return this;
  }

  @Override
  public RedisClient clusterKeyslot(String key, Handler<AsyncResult<Long>> handler) {
    client. clusterKeyslot(key, handler);
    return this;
  }

  @Override
  public RedisClient clusterMeet(String ip, long port, Handler<AsyncResult<Void>> handler) {
    client. clusterMeet(ip, port, handler);
    return this;
  }

  @Override
  public RedisClient clusterNodes(Handler<AsyncResult<JsonArray>> handler) {
    client.clusterNodes(handler);
    return this;
  }

  @Override
  public RedisClient clusterReplicate(String nodeId, Handler<AsyncResult<Void>> handler) {
    client.clusterReplicate(nodeId, handler);
    return this;
  }

  @Override
  public RedisClient clusterReset(Handler<AsyncResult<Void>> handler) {
    client.clusterReset(handler);
    return this;
  }

  @Override
  public RedisClient clusterResetWithOptions(ResetOptions options,
                                             Handler<AsyncResult<Void>> handler) {
    client.clusterResetWithOptions(options, handler);
    return this;
  }

  @Override
  public RedisClient clusterSaveconfig(Handler<AsyncResult<Void>> handler) {
    client.clusterSaveconfig(handler);
    return this;
  }

  @Override
  public RedisClient clusterSetConfigEpoch(long epoch, Handler<AsyncResult<Void>> handler) {
    client.clusterSetConfigEpoch(epoch, handler);
    return this;
  }

  @Override
  public RedisClient clusterSetslot(long slot, SlotCmd subcommand,
                                    Handler<AsyncResult<Void>> handler) {
    client.clusterSetslot(slot, subcommand, handler);
    return this;
  }

  @Override
  public RedisClient clusterSetslotWithNode(long slot, SlotCmd subcommand, String nodeId,
                                            Handler<AsyncResult<Void>> handler) {
    client.clusterSetslotWithNode(slot, subcommand, nodeId, handler);
    return this;
  }

  @Override
  public RedisClient clusterSlaves(String nodeId, Handler<AsyncResult<JsonArray>> handler) {
    client.clusterSlaves(nodeId, handler);
    return this;
  }

  @Override
  public RedisClient clusterSlots(Handler<AsyncResult<JsonArray>> handler) {
    client.clusterSlots(handler);
    return this;
  }

  @Override
  public RedisClient command(Handler<AsyncResult<JsonArray>> handler) {
    client.command(handler);
    return this;
  }

  @Override
  public RedisClient commandCount(Handler<AsyncResult<Long>> handler) {
    client.commandCount(handler);
    return this;
  }

  @Override
  public RedisClient commandGetkeys(Handler<AsyncResult<JsonArray>> handler) {
    client.commandGetkeys(handler);
    return this;
  }

  @Override
  public RedisClient commandInfo(List<String> commands, Handler<AsyncResult<JsonArray>> handler) {
    client.commandInfo(commands, handler);
    return this;
  }

  @Override
  public RedisClient configGet(String parameter, Handler<AsyncResult<JsonArray>> handler) {
    client.configGet(parameter, handler);
    return this;
  }

  @Override
  public RedisClient configRewrite(Handler<AsyncResult<String>> handler) {
    client.configRewrite(handler);
    return this;
  }

  @Override
  public RedisClient configSet(String parameter, String value,
                               Handler<AsyncResult<String>> handler) {
    client.configSet(parameter, value, handler);
    return this;
  }

  @Override
  public RedisClient configResetstat(Handler<AsyncResult<String>> handler) {
    client.configResetstat(handler);
    return this;
  }

  @Override
  public RedisClient dbsize(Handler<AsyncResult<Long>> handler) {
    client.dbsize(handler);
    return this;
  }

  @Override
  public RedisClient debugObject(String key, Handler<AsyncResult<String>> handler) {
    client.debugObject(key, handler);
    return this;
  }

  @Override
  public RedisClient debugSegfault(Handler<AsyncResult<String>> handler) {
    client.debugSegfault(handler);
    return this;
  }

  @Override
  public RedisClient decr(String key, Handler<AsyncResult<Long>> handler) {
    client.decr(key, handler);
    return this;
  }

  @Override
  public RedisClient decrby(String key, long decrement, Handler<AsyncResult<Long>> handler) {
    client.decrby(key, decrement, handler);
    return this;
  }

  @Override
  public RedisClient del(String key, Handler<AsyncResult<Long>> handler) {
    client.del(key, handler);
    return this;
  }

  @Override
  public RedisClient delMany(List<String> keys, Handler<AsyncResult<Long>> handler) {
    client.delMany(keys, handler);
    return this;
  }

  @Override
  public RedisClient dump(String key, Handler<AsyncResult<String>> handler) {
    client.dump(key, handler);
    return this;
  }

  @Override
  public RedisClient echo(String message, Handler<AsyncResult<String>> handler) {
    client.echo(message, handler);
    return this;
  }

  @Override
  public RedisClient eval(String script, List<String> keys, List<String> args,
                          Handler<AsyncResult<JsonArray>> handler) {
    client.eval(script, keys, args, handler);
    return this;
  }

  @Override
  public RedisClient evalsha(String sha1, List<String> keys, List<String> values,
                             Handler<AsyncResult<JsonArray>> handler) {
    client.evalsha(sha1, keys, values, handler);
    return this;
  }

  @Override
  public RedisClient exists(String key, Handler<AsyncResult<Long>> handler) {
    client.exists(key, handler);
    return this;
  }

  @Override
  public RedisClient expire(String key, long seconds, Handler<AsyncResult<Long>> handler) {
    client.expire(key, seconds, handler);
    return this;
  }

  @Override
  public RedisClient expireat(String key, long seconds, Handler<AsyncResult<Long>> handler) {
    client.expireat(key, seconds, handler);
    return this;
  }

  @Override
  public RedisClient flushall(Handler<AsyncResult<String>> handler) {
    client.flushall(handler);
    return this;
  }

  @Override
  public RedisClient flushdb(Handler<AsyncResult<String>> handler) {
    client.flushdb(handler);
    return this;
  }

  @Override
  public RedisClient get(String key, Handler<AsyncResult<String>> handler) {
    client.get(key, handler);
    return this;
  }

  @Override
  public RedisClient getBinary(String key, Handler<AsyncResult<Buffer>> handler) {
    client.getBinary(key, handler);
    return this;
  }

  @Override
  public RedisClient getbit(String key, long offset, Handler<AsyncResult<Long>> handler) {
    client.getbit(key, offset, handler);
    return this;
  }

  @Override
  public RedisClient getrange(String key, long start, long end,
                              Handler<AsyncResult<String>> handler) {
    client.getrange(key, start, end, handler);
    return this;
  }

  @Override
  public RedisClient getset(String key, String value, Handler<AsyncResult<String>> handler) {
    client.getset(key, value, handler);
    return this;
  }

  @Override
  public RedisClient hdel(String key, String field, Handler<AsyncResult<Long>> handler) {
    client.hdel(key, field, handler);
    return this;
  }

  @Override
  public RedisClient hdelMany(String key, List<String> fields, Handler<AsyncResult<Long>> handler) {
    client.hdelMany(key, fields, handler);
    return this;
  }

  @Override
  public RedisClient hexists(String key, String field, Handler<AsyncResult<Long>> handler) {
    client.hexists(key, field, handler);
    return this;
  }

  @Override
  public RedisClient hget(String key, String field, Handler<AsyncResult<String>> handler) {
    client.hget(key, field, handler);
    return this;
  }

  @Override
  public RedisClient hgetall(String key, Handler<AsyncResult<JsonObject>> handler) {
    client.hgetall(key, handler);
    return this;
  }

  @Override
  public RedisClient hincrby(String key, String field, long increment,
                             Handler<AsyncResult<Long>> handler) {
    client.hincrby(key, field, increment, handler);
    return this;
  }

  @Override
  public RedisClient hincrbyfloat(String key, String field, double increment,
                                  Handler<AsyncResult<String>> handler) {
    client.hincrbyfloat(key, field, increment, handler);
    return this;
  }

  @Override
  public RedisClient hkeys(String key, Handler<AsyncResult<JsonArray>> handler) {
    client.hkeys(key, handler);
    return this;
  }

  @Override
  public RedisClient hlen(String key, Handler<AsyncResult<Long>> handler) {
    client.hlen(key, handler);
    return this;
  }

  @Override
  public RedisClient hmget(String key, List<String> fields,
                           Handler<AsyncResult<JsonArray>> handler) {
    client.hmget(key, fields, handler);
    return this;
  }

  @Override
  public RedisClient hmset(String key, JsonObject values, Handler<AsyncResult<String>> handler) {
    client.hmset(key, values, handler);
    return this;
  }

  @Override
  public RedisClient hset(String key, String field, String value,
                          Handler<AsyncResult<Long>> handler) {
    client.hset(key, field, value, handler);
    return this;
  }

  @Override
  public RedisClient hsetnx(String key, String field, String value,
                            Handler<AsyncResult<Long>> handler) {
    client.hsetnx(key, field, value, handler);
    return this;
  }

  @Override
  public RedisClient hvals(String key, Handler<AsyncResult<JsonArray>> handler) {
    client.hvals(key, handler);
    return this;
  }

  @Override
  public RedisClient incr(String key, Handler<AsyncResult<Long>> handler) {
    client.incr(key, handler);
    return this;
  }

  @Override
  public RedisClient incrby(String key, long increment, Handler<AsyncResult<Long>> handler) {
    client.incrby(key, increment, handler);
    return this;
  }

  @Override
  public RedisClient incrbyfloat(String key, double increment,
                                 Handler<AsyncResult<String>> handler) {
    client.incrbyfloat(key, increment, handler);
    return this;
  }

  @Override
  public RedisClient info(Handler<AsyncResult<JsonObject>> handler) {
    client.info(handler);
    return this;
  }

  @Override
  public RedisClient infoSection(String section, Handler<AsyncResult<JsonObject>> handler) {
    client.infoSection(section, handler);
    return this;
  }

  @Override
  public RedisClient keys(String pattern, Handler<AsyncResult<JsonArray>> handler) {
    client.keys(pattern, handler);
    return this;
  }

  @Override
  public RedisClient lastsave(Handler<AsyncResult<Long>> handler) {
    client.lastsave(handler);
    return this;
  }

  @Override
  public RedisClient lindex(String key, int index, Handler<AsyncResult<String>> handler) {
    client.lindex(key, index, handler);
    return this;
  }

  @Override
  public RedisClient linsert(String key, InsertOptions option, String pivot, String value,
                             Handler<AsyncResult<Long>> handler) {
    client.linsert(key, option, pivot, value, handler);
    return this;
  }

  @Override
  public RedisClient llen(String key, Handler<AsyncResult<Long>> handler) {
    client.llen(key, handler);
    return this;
  }

  @Override
  public RedisClient lpop(String key, Handler<AsyncResult<String>> handler) {
    client.lpop(key, handler);
    return this;
  }

  @Override
  public RedisClient lpushMany(String key, List<String> values,
                               Handler<AsyncResult<Long>> handler) {
    client.lpushMany(key, values, handler);
    return this;
  }

  @Override
  public RedisClient lpush(String key, String value, Handler<AsyncResult<Long>> handler) {
    client.lpush(key, value, handler);
    return this;
  }

  @Override
  public RedisClient lpushx(String key, String value, Handler<AsyncResult<Long>> handler) {
    client.lpushx(key, value, handler);
    return this;
  }

  @Override
  public RedisClient lrange(String key, long from, long to,
                            Handler<AsyncResult<JsonArray>> handler) {
    client.lrange(key, from, to, handler);
    return this;
  }

  @Override
  public RedisClient lrem(String key, long count, String value,
                          Handler<AsyncResult<Long>> handler) {
    client.lrem(key, count, value, handler);
    return this;
  }

  @Override
  public RedisClient lset(String key, long index, String value,
                          Handler<AsyncResult<String>> handler) {
    client.lset(key, index, value, handler);
    return this;
  }

  @Override
  public RedisClient ltrim(String key, long from, long to, Handler<AsyncResult<String>> handler) {
    client.ltrim(key, from, to, handler);
    return this;
  }

  @Override
  public RedisClient mget(String key, Handler<AsyncResult<JsonArray>> handler) {
    client.mget(key, handler);
    return this;
  }

  @Override
  public RedisClient mgetMany(List<String> keys, Handler<AsyncResult<JsonArray>> handler) {
    client.mgetMany(keys, handler);
    return this;
  }

  @Override
  public RedisClient migrate(String host, int port, String key, int destdb, long timeout,
                             MigrateOptions options, Handler<AsyncResult<String>> handler) {
    client.migrate(host, port, key, destdb, timeout, options, handler);
    return this;
  }

  @Override
  public RedisClient monitor(Handler<AsyncResult<Void>> handler) {
    client.monitor(handler);
    return this;
  }

  @Override
  public RedisClient move(String key, int destdb, Handler<AsyncResult<Long>> handler) {
    client.move(key, destdb, handler);
    return this;
  }

  @Override
  public RedisClient mset(JsonObject keyvals, Handler<AsyncResult<String>> handler) {
    client.mset(keyvals, handler);
    return this;
  }

  @Override
  public RedisClient msetnx(JsonObject keyvals, Handler<AsyncResult<Long>> handler) {
    client.msetnx(keyvals, handler);
    return this;
  }

  @Override
  public RedisClient object(String key, ObjectCmd cmd, Handler<AsyncResult<Void>> handler) {
    client.object(key, cmd, handler);
    return this;
  }

  @Override
  public RedisClient persist(String key, Handler<AsyncResult<Long>> handler) {
    client.persist(key, handler);
    return this;
  }

  @Override
  public RedisClient pexpire(String key, long millis, Handler<AsyncResult<Long>> handler) {
    client.pexpire(key, millis, handler);
    return this;
  }

  @Override
  public RedisClient pexpireat(String key, long millis, Handler<AsyncResult<Long>> handler) {
    client.pexpireat(key, millis, handler);
    return this;
  }

  @Override
  public RedisClient pfadd(String key, String element, Handler<AsyncResult<Long>> handler) {
    client.pfadd(key, element, handler);
    return this;
  }

  @Override
  public RedisClient pfaddMany(String key, List<String> elements,
                               Handler<AsyncResult<Long>> handler) {
    client.pfaddMany(key, elements, handler);
    return this;
  }

  @Override
  public RedisClient pfcount(String key, Handler<AsyncResult<Long>> handler) {
    client.pfcount(key, handler);
    return this;
  }

  @Override
  public RedisClient pfcountMany(List<String> keys, Handler<AsyncResult<Long>> handler) {
    client.pfcountMany(keys, handler);
    return this;
  }

  @Override
  public RedisClient pfmerge(String destkey, List<String> keys,
                             Handler<AsyncResult<String>> handler) {
    client.pfmerge(destkey, keys, handler);
    return this;
  }

  @Override
  public RedisClient ping(Handler<AsyncResult<String>> handler) {
    client.ping(handler);
    return this;
  }

  @Override
  public RedisClient psetex(String key, long millis, String value,
                            Handler<AsyncResult<Void>> handler) {
    client.psetex(key, millis, value, handler);
    return this;
  }

  @Override
  public RedisClient psubscribe(String pattern, Handler<AsyncResult<JsonArray>> handler) {
    client.psubscribe(pattern, handler);
    return this;
  }

  @Override
  public RedisClient psubscribeMany(List<String> patterns,
                                    Handler<AsyncResult<JsonArray>> handler) {
    client.psubscribeMany(patterns, handler);
    return this;
  }

  @Override
  public RedisClient pubsubChannels(String pattern, Handler<AsyncResult<JsonArray>> handler) {
    client.pubsubChannels(pattern, handler);
    return this;
  }

  @Override
  public RedisClient pubsubNumsub(List<String> channels, Handler<AsyncResult<JsonArray>> handler) {
    client.pubsubNumsub(channels, handler);
    return this;
  }

  @Override
  public RedisClient pubsubNumpat(Handler<AsyncResult<Long>> handler) {
    client.pubsubNumpat(handler);
    return this;
  }

  @Override
  public RedisClient pttl(String key, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient publish(String channel, String message, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient punsubscribe(List<String> patterns, Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient randomkey(Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient rename(String key, String newkey, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient renamenx(String key, String newkey, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient restore(String key, long millis, String serialized,
                             Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient role(Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient rpop(String key, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient rpoplpush(String key, String destkey, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient rpushMany(String key, List<String> values,
                               Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient rpush(String key, String value, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient rpushx(String key, String value, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient sadd(String key, String member, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient saddMany(String key, List<String> members,
                              Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient save(Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient scard(String key, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient scriptExists(String script, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient scriptExistsMany(List<String> scripts,
                                      Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient scriptFlush(Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient scriptKill(Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient scriptLoad(String script, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient sdiff(String key, List<String> cmpkeys,
                           Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient sdiffstore(String destkey, String key, List<String> cmpkeys,
                                Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient select(int dbindex, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient set(String key, String value, Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient setWithOptions(String key, String value, SetOptions options,
                                    Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient setBinary(String key, Buffer value, Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient setBinaryWithOptions(String key, Buffer value, SetOptions options,
                                          Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient setbit(String key, long offset, int bit, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient setex(String key, long seconds, String value,
                           Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient setnx(String key, String value, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient setrange(String key, int offset, String value,
                              Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient sinter(List<String> keys, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient sinterstore(String destkey, List<String> keys,
                                 Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient sismember(String key, String member, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient slaveof(String host, int port, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient slaveofNoone(Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient slowlogGet(int limit, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient slowlogLen(Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient slowlogReset(Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient smembers(String key, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient smove(String key, String destkey, String member,
                           Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient sort(String key, SortOptions options,
                          Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient spop(String key, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient spopMany(String key, int count, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient srandmember(String key, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient srandmemberCount(String key, int count,
                                      Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient srem(String key, String member, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient sremMany(String key, List<String> members,
                              Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient strlen(String key, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient subscribe(String channel, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient subscribeMany(List<String> channels, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient sunion(List<String> keys, Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient sunionstore(String destkey, List<String> keys,
                                 Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient sync(Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient time(Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisTransaction transaction() {
    return client.transaction();
  }

  @Override
  public RedisClient ttl(String key, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient type(String key, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient unsubscribe(List<String> channels, Handler<AsyncResult<Void>> handler) {
    return this;
  }

  @Override
  public RedisClient wait(long numSlaves, long timeout, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient zadd(String key, double score, String member,
                          Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zaddMany(String key, Map<String, Double> members,
                              Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zcard(String key, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zcount(String key, double min, double max,
                            Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zincrby(String key, double increment, String member,
                             Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient zinterstore(String destkey, List<String> sets, AggregateOptions options,
                                 Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zinterstoreWeighed(String destkey, Map<String, Double> sets,
                                        AggregateOptions options,
                                        Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zlexcount(String key, String min, String max,
                               Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zrange(String key, long start, long stop,
                            Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrangeWithOptions(String key, long start, long stop, RangeOptions options,
                                       Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrangebylex(String key, String min, String max, LimitOptions options,
                                 Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrangebyscore(String key, String min, String max, RangeLimitOptions options,
                                   Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrank(String key, String member, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zrem(String key, String member, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zremMany(String key, List<String> members,
                              Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zremrangebylex(String key, String min, String max,
                                    Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zremrangebyrank(String key, long start, long stop,
                                     Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zremrangebyscore(String key, String min, String max,
                                      Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zrevrange(String key, long start, long stop, RangeOptions options,
                               Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrevrangebylex(String key, String max, String min, LimitOptions options,
                                    Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrevrangebyscore(String key, String max, String min, RangeLimitOptions options,
                                      Handler<AsyncResult<JsonArray>> handler) {
    return this;
  }

  @Override
  public RedisClient zrevrank(String key, String member, Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zscore(String key, String member, Handler<AsyncResult<String>> handler) {
    return this;
  }

  @Override
  public RedisClient zunionstore(String destkey, List<String> sets, AggregateOptions options,
                                 Handler<AsyncResult<Long>> handler) {
    return this;
  }

  @Override
  public RedisClient zunionstoreWeighed(String key, Map<String, Double> sets,
                                        AggregateOptions options,
                                        Handler<AsyncResult<Long>> handler) {
    client.zunionstoreWeighed(key, sets, options, handler);
    return this;
  }

  @Override
  public RedisClient scan(String cursor, ScanOptions options,
                          Handler<AsyncResult<JsonArray>> handler) {
    client.scan(cursor, options, handler);
    return this;
  }

  @Override
  public RedisClient sscan(String key, String cursor, ScanOptions options,
                           Handler<AsyncResult<JsonArray>> handler) {
    client.sscan(key, cursor, options, handler);
    return this;
  }

  @Override
  public RedisClient hscan(String key, String cursor, ScanOptions options,
                           Handler<AsyncResult<JsonArray>> handler) {
    client.hscan(key, cursor, options, handler);
    return this;
  }

  @Override
  public RedisClient zscan(String key, String cursor, ScanOptions options,
                           Handler<AsyncResult<JsonArray>> handler) {
    client.zscan(key, cursor, options, handler);
    return this;
  }

  @Override
  public RedisClient geoadd(String key, double longitude, double latitude, String member,
                            Handler<AsyncResult<Long>> handler) {
    client.geoadd(key, longitude, latitude, member, handler);
    return this;
  }

  @Override
  public RedisClient geoaddMany(String key, List<GeoMember> members,
                                Handler<AsyncResult<Long>> handler) {
    client.geoaddMany(key, members, handler);
    return this;
  }

  @Override
  public RedisClient geohash(String key, String member, Handler<AsyncResult<JsonArray>> handler) {
    client.geohash(key, member, handler);
    return this;
  }

  @Override
  public RedisClient geohashMany(String key, List<String> members,
                                 Handler<AsyncResult<JsonArray>> handler) {
    client.geohashMany(key, members, handler);
    return this;
  }

  @Override
  public RedisClient geopos(String key, String member, Handler<AsyncResult<JsonArray>> handler) {
    client.geopos(key, member, handler);
    return this;
  }

  @Override
  public RedisClient geoposMany(String key, List<String> members,
                                Handler<AsyncResult<JsonArray>> handler) {
    client.geoposMany(key, members, handler);
    return this;
  }

  @Override
  public RedisClient geodist(String key, String member1, String member2,
                             Handler<AsyncResult<String>> handler) {
    client.geodist(key, member1, member2, handler);
    return this;
  }

  @Override
  public RedisClient geodistWithUnit(String key, String member1, String member2, GeoUnit unit,
                                     Handler<AsyncResult<String>> handler) {
    client.geodistWithUnit(key, member1, member2, unit, handler);
    return this;
  }

  @Override
  public RedisClient georadius(String key, double longitude, double latitude, double radius,
                               GeoUnit unit, Handler<AsyncResult<JsonArray>> handler) {
    client.georadius(key, longitude, latitude, radius, unit, handler);
    return this;
  }

  @Override
  public RedisClient georadiusWithOptions(String key, double longitude, double latitude,
                                          double radius, GeoUnit unit, GeoRadiusOptions options,
                                          Handler<AsyncResult<JsonArray>> handler) {
    client.georadiusWithOptions(key, longitude, latitude, radius, unit, options, handler);
    return this;
  }

  @Override
  public RedisClient georadiusbymember(String key, String member, double radius, GeoUnit unit,
                                       Handler<AsyncResult<JsonArray>> handler) {
    client.georadiusbymember(key, member, radius, unit, handler);
    return this;
  }

  @Override
  public RedisClient georadiusbymemberWithOptions(String key, String member, double radius,
                                                  GeoUnit unit, GeoRadiusOptions options,
                                                  Handler<AsyncResult<JsonArray>> handler) {
    client.georadiusbymemberWithOptions(key, member, radius, unit, options, handler);
    return this;
  }

  @Override
  public RedisClient clientReply(ClientReplyOptions options, Handler<AsyncResult<String>> handler) {
    client.clientReply(options, handler);
    return this;
  }

  @Override
  public RedisClient hstrlen(String key, String field, Handler<AsyncResult<Long>> handler) {
    client.hstrlen(key, field, handler);
    return this;
  }

  @Override
  public RedisClient touch(String key, Handler<AsyncResult<Long>> handler) {
    client.touch(key, handler);
    return this;
  }

  @Override
  public RedisClient touchMany(List<String> keys, Handler<AsyncResult<Long>> handler) {
    client.touchMany(keys, handler);
    return this;
  }

  @Override
  public RedisClient scriptDebug(ScriptDebugOptions scriptDebugOptions,
                                 Handler<AsyncResult<String>> handler) {
    client.scriptDebug(scriptDebugOptions, handler);
    return this;
  }

  @Override
  public RedisClient bitfield(String key, BitFieldOptions bitFieldOptions,
                              Handler<AsyncResult<JsonArray>> handler) {
    client.bitfield(key, bitFieldOptions, handler);
    return this;
  }

  @Override
  public RedisClient bitfieldWithOverflow(String key, BitFieldOptions commands,
                                          BitFieldOverflowOptions overflow,
                                          Handler<AsyncResult<JsonArray>> handler) {
    client.bitfieldWithOverflow(key, commands, overflow, handler);
    return this;
  }

  RedisClient client() {
    return client;
  }

}