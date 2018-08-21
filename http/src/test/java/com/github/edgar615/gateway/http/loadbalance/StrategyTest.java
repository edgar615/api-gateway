package com.github.edgar615.gateway.http.loadbalance;

import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 17-5-6.
 */
public class StrategyTest {
    List<Record> instances = new ArrayList<>();

    @Before
    public void setUp() {
        instances.clear();
        instances.add(HttpEndpoint.createRecord("test", "localhost", 8081, "/")
                              .setRegistration("a"));
        instances.add(HttpEndpoint.createRecord("test", "localhost", 8082, "/")
                              .setRegistration("b"));
        instances.add(HttpEndpoint.createRecord("test", "localhost", 8083, "/")
                              .setRegistration("c"));
    }

    protected List<String> select3000(ChooseStrategy strategy) {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < 3000; i++) {
            Record instance = strategy.get(instances);
            selected.add(instance.getRegistration());
        }
        return selected;
    }

}
