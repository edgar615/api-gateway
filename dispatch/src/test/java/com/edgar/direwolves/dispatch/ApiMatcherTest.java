package com.edgar.direwolves.dispatch;

import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.definition.ApiDefinitionRegistry;
import com.edgar.direwolves.definition.HttpEndpoint;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

/**
 * Created by edgar on 16-9-12.
 */
public class ApiMatcherTest {

    ApiDefinitionRegistry registry;

    @Before
    public void setUp() {
        registry = ApiDefinitionRegistry.instance();
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setArray(true).build();

        ApiDefinition apiDefinition = ApiDefinition.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/([\\d+]+)")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();
        registry.add(apiDefinition);

        apiDefinition = ApiDefinition.builder().setName("delete_device")
                .setMethod(HttpMethod.DELETE)
                .setPath("devices/([\\d+]+)")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();
        registry.add(apiDefinition);

        apiDefinition = ApiDefinition.builder().setName("update_device")
                .setMethod(HttpMethod.PUT)
                .setPath("devices/([\\d+]+)")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();
        registry.add(apiDefinition);

        apiDefinition = ApiDefinition.builder().setName("get_devices")
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();
        registry.add(apiDefinition);

        apiDefinition = ApiDefinition.builder().setName("add_device")
                .setMethod(HttpMethod.POST)
                .setPath("/devices")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();
        registry.add(apiDefinition);

        apiDefinition = ApiDefinition.builder().setName("get_part")
                .setMethod(HttpMethod.GET)
                .setPath("devices/([\\d+]+)/parts/([\\d+]+)")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();
        registry.add(apiDefinition);
    }

    @After
    public void clear() {
        registry.remove(null);
    }

    @Test
    public void testMatch() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices")
                .setMethod(HttpMethod.POST)
                .setBody(new JsonObject())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                .findFirst();
        Assert.assertTrue(optional.isPresent());
    }

    @Test
    public void testNotMatch() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices")
                .setMethod(HttpMethod.PUT)
                .setBody(new JsonObject())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testOneParam() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices/1")
                .setMethod(HttpMethod.GET)
                .setBody(new JsonObject())
                .setParams(ArrayListMultimap.create())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(apiContext.getParams().containsKey("param1"));
        Assert.assertEquals("1", Iterables.get(apiContext.getParams().get("param1"), 0));
    }

    @Test
    public void testTwoParam() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices/1/parts/2")
                .setMethod(HttpMethod.GET)
                .setBody(new JsonObject())
                .setParams(ArrayListMultimap.create())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertTrue(optional.isPresent());
        Assert.assertTrue(apiContext.getParams().containsKey("param1"));
        Assert.assertEquals("1", Iterables.get(apiContext.getParams().get("param1"), 0));
        Assert.assertEquals("2", Iterables.get(apiContext.getParams().get("param2"), 0));
    }

    @Test
    public void testUnMatcher() {
        ApiContext apiContext = ApiContext.builder().setPath("/devices/abc")
                .setMethod(HttpMethod.GET)
                .setBody(new JsonObject())
                .setParams(ArrayListMultimap.create())
                .build();
        Optional<ApiDefinition> optional =
                registry.getDefinitions().stream().filter(definition -> ApiMatcher.instance().apply(apiContext, definition))
                        .findFirst();
        Assert.assertFalse(optional.isPresent());
    }
}
