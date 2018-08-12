package com.github.edgar615.gateway.core.plugin.predicate;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import com.google.common.base.Preconditions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredicatePluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return PredicatePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new PredicatePlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("predicate") instanceof JsonObject) {
      PredicatePlugin plugin = new PredicatePlugin();
      JsonObject predicate = jsonObject.getJsonObject("predicate");
      if (predicate.getValue("before") instanceof String) {
        plugin.add(new BeforePredicate(predicate.getString("before")));
      }
      if (predicate.getValue("after") instanceof String) {
        plugin.add(new BeforePredicate(predicate.getString("after")));
      }
      if (predicate.getValue("between") instanceof JsonArray) {
        JsonArray between = predicate.getJsonArray("between");
        Preconditions.checkArgument(between.size() == 2, "between must has 2 element");
        plugin.add(new BetweenPredicate(between.getString(0), between.getString(1)));
      }
      if (predicate.getValue("header") instanceof JsonObject) {
        plugin.addAll(headerPredicate(predicate.getJsonObject("header")));
      }
      if (predicate.getValue("query") instanceof JsonObject) {
        plugin.addAll(queryPredicate(predicate.getJsonObject("query")));
      }
      if (predicate.getValue("remoteAddr") instanceof JsonObject) {
        plugin.addAll(remoteAddrPredicate(predicate.getJsonObject("remoteAddr")));
      }
      return plugin;
    }
    return null;
  }

  private List<ApiPredicate> headerPredicate(JsonObject headerConfig) {
    List<ApiPredicate> predicates = new ArrayList<>();
    if (headerConfig.getValue("contains") instanceof JsonArray) {
      JsonArray contains = headerConfig.getJsonArray("contains");
      predicates.add(new HeaderContainsPredicate(contains.getList()));
    }
    if (headerConfig.getValue("equals") instanceof JsonObject) {
      JsonObject equals = headerConfig.getJsonObject("equals");
      predicates.add(new HeaderEqualsPredicate(transform(equals)));
    }
    if (headerConfig.getValue("regex") instanceof JsonObject) {
      JsonObject regex = headerConfig.getJsonObject("regex");
      predicates.add(new HeaderRegexPredicate(transform(regex)));
    }
    return predicates;
  }

  private List<ApiPredicate> queryPredicate(JsonObject queryConfig) {
    List<ApiPredicate> predicates = new ArrayList<>();
    if (queryConfig.getValue("contains") instanceof JsonArray) {
      JsonArray contains = queryConfig.getJsonArray("contains");
      predicates.add(new QueryContainsPredicate(contains.getList()));
    }
    if (queryConfig.getValue("equals") instanceof JsonObject) {
      JsonObject equals = queryConfig.getJsonObject("equals");
      predicates.add(new QueryEqualsPredicate(transform(equals)));
    }
    if (queryConfig.getValue("regex") instanceof JsonObject) {
      JsonObject regex = queryConfig.getJsonObject("regex");
      predicates.add(new QueryRegexPredicate(transform(regex)));
    }
    return predicates;
  }

  private List<ApiPredicate> remoteAddrPredicate(JsonObject remoteAddrConfig) {
    List<ApiPredicate> predicates = new ArrayList<>();
    if (remoteAddrConfig.getValue("appoint") instanceof JsonArray) {
      JsonArray ipArray = remoteAddrConfig.getJsonArray("appoint", new JsonArray());
      RemoteAddrAppointPredicate predicate = new RemoteAddrAppointPredicate(ipArray.getList());
      predicates.add(predicate);
    }
    if (remoteAddrConfig.getValue("hash") instanceof JsonObject) {
      JsonObject jsonObject = remoteAddrConfig.getJsonObject("hash");
      int start = jsonObject.getInteger("start", 0);
      int end = jsonObject.getInteger("end", 100);
      predicates.add(new RemoteAddrHashPredicate(start, end));
    }
    if (remoteAddrConfig.getValue("range") instanceof JsonObject) {
      JsonObject jsonObject = remoteAddrConfig.getJsonObject("range");
      long start = jsonObject.getLong("start", 0l);
      long end = jsonObject.getLong("end", Long.MAX_VALUE);
      predicates.add(new RemoteAddrRangePredicate(start, end));
    }
    return predicates;
  }

  private Map<String, String> transform(JsonObject jsonObject) {
    Map<String, String> map = new HashMap<>();
    for (String key : jsonObject.fieldNames()) {
      map.put(key, jsonObject.getString(key));
    }
    return map;
  }

  @Override
  public JsonObject encode(ApiPlugin apiPlugin) {
    PredicatePlugin predicatePlugin = (PredicatePlugin) apiPlugin;
    JsonObject jsonObject = new JsonObject();
    predicatePlugin.predicates().forEach(predicate -> {
      if (predicate instanceof BeforePredicate) {
        jsonObject.put("before", ((BeforePredicate) predicate).dateTime());
      }
      if (predicate instanceof AfterPredicate) {
        jsonObject.put("after", ((AfterPredicate) predicate).dateTime());
      }
      if (predicate instanceof BetweenPredicate) {
        JsonArray between = new JsonArray().add(((BetweenPredicate) predicate).startDateTime())
                .add(((BetweenPredicate) predicate).endDateTime());
        jsonObject.put("between", between);
      }
      JsonObject header = encodeHeader(predicate);
      if (!header.isEmpty() && jsonObject.getValue("header") == null) {
        jsonObject.put("header", header);
      } else  if (!header.isEmpty() && jsonObject.getValue("header") instanceof JsonObject) {
        JsonObject eldHeader = jsonObject.getJsonObject("header");
        jsonObject.put("header", eldHeader.mergeIn(header));
      }
      JsonObject query = encodeQuery(predicate);
      if (!query.isEmpty() && jsonObject.getValue("query") == null) {
        jsonObject.put("query", query);
      } else  if (!query.isEmpty() && jsonObject.getValue("query") instanceof JsonObject) {
        JsonObject eldQuery = jsonObject.getJsonObject("query");
        jsonObject.put("query", eldQuery.mergeIn(query));
      }
      JsonObject remoteAddr = encodeRemoteAddr(predicate);
      if (!remoteAddr.isEmpty() && jsonObject.getValue("remoteAddr") == null) {
        jsonObject.put("remoteAddr", remoteAddr);
      } else  if (!remoteAddr.isEmpty() && jsonObject.getValue("remoteAddr") instanceof JsonObject) {
        JsonObject eldRemoteAddr = jsonObject.getJsonObject("remoteAddr");
        jsonObject.put("remoteAddr", eldRemoteAddr.mergeIn(remoteAddr));
      }
    });
    return jsonObject;
  }

  private JsonObject encodeHeader(ApiPredicate predicate) {
    JsonObject header = new JsonObject();
    if (predicate instanceof HeaderContainsPredicate) {
      HeaderContainsPredicate containsPredicate = (HeaderContainsPredicate) predicate;
      header.put("contains", containsPredicate.headers());
    }
    if (predicate instanceof HeaderEqualsPredicate) {
      HeaderEqualsPredicate equalsPredicate = (HeaderEqualsPredicate) predicate;
      header.put("equals", equalsPredicate.headers());
    }
    if (predicate instanceof HeaderRegexPredicate) {
      HeaderRegexPredicate regexPredicate = (HeaderRegexPredicate) predicate;
      header.put("regex", regexPredicate.headers());
    }
    return header;
  }

  private JsonObject encodeQuery(ApiPredicate predicate) {
    JsonObject query = new JsonObject();
    if (predicate instanceof QueryContainsPredicate) {
      QueryContainsPredicate containsPredicate = (QueryContainsPredicate) predicate;
      query.put("contains", containsPredicate.query());
    }
    if (predicate instanceof QueryEqualsPredicate) {
      QueryEqualsPredicate equalsPredicate = (QueryEqualsPredicate) predicate;
      query.put("equals", equalsPredicate.query());
    }
    if (predicate instanceof QueryRegexPredicate) {
      QueryRegexPredicate regexPredicate = (QueryRegexPredicate) predicate;
      query.put("regex", regexPredicate.query());
    }
    return query;
  }

  private JsonObject encodeRemoteAddr(ApiPredicate predicate) {
    JsonObject remoteAddr = new JsonObject();
    if (predicate instanceof RemoteAddrAppointPredicate) {
      RemoteAddrAppointPredicate appointPredicate = (RemoteAddrAppointPredicate) predicate;
      remoteAddr.put("appoint", appointPredicate.appoint());
    }
    if (predicate instanceof RemoteAddrHashPredicate) {
      RemoteAddrHashPredicate hashPredicate = (RemoteAddrHashPredicate) predicate;
      JsonObject hash = new JsonObject()
              .put("start", hashPredicate.start())
              .put("end", hashPredicate.end());
      remoteAddr.put("hash", hash);
    }
    if (predicate instanceof RemoteAddrRangePredicate) {
      RemoteAddrRangePredicate rangePredicate = (RemoteAddrRangePredicate) predicate;
      JsonObject range = new JsonObject()
              .put("start", rangePredicate.start())
              .put("end", rangePredicate.end());
      remoteAddr.put("range", range);
    }
    return remoteAddr;
  }
}
