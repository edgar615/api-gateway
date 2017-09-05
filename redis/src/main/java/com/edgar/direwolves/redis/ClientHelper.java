package com.edgar.direwolves.redis;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.redis.RedisClient;

/**
 * This class handles sharing the client instances by using a local shared map.
 * 用于RedisClient的共享，代码参考了MySQL / PostgreSQL client中
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClientHelper {

  private static final String DEFAULT_POOL_NAME = "DEFAULT_REDIS_POOL";

  private static final String DS_LOCAL_MAP_NAME_BASE = "__vertx.redis.pools";

  public static RedisClient createShared(Vertx vertx, JsonObject config) {
    return createShared(vertx, config, DEFAULT_POOL_NAME);
  }

  public static RedisClient createShared(Vertx vertx, JsonObject config, String poolName) {
    synchronized (vertx) {
      LocalMap<String, ClientHolder> map = vertx.sharedData().getLocalMap(DS_LOCAL_MAP_NAME_BASE);

      ClientHolder theHolder = map.get(poolName);
      if (theHolder == null) {
        theHolder =
                new ClientHolder(vertx, config, () -> removeFromMap(vertx, map, poolName));
        map.put(poolName, theHolder);
      } else {
        theHolder.incRefCount();
      }
      return new ClientWrapper(theHolder);
    }
  }

  private static void removeFromMap(Vertx vertx, LocalMap<String, ClientHolder> map,
                                    String poolName) {
    synchronized (vertx) {
      map.remove(poolName);
      if (map.isEmpty()) {
        map.close();
      }
    }
  }

}