package com.edgar.direwolves.definition;

import com.google.common.collect.Lists;
import io.vertx.core.http.HttpMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by Edgar on 2016/4/11.
 *
 * @author Edgar  Date 2016/4/11
 */
public class ApiDefinitionRegistryTest {

    ApiDefinitionRegistry registry;
    @Before
    public void setUp() {
        registry = ApiDefinitionRegistry.instance();
    }

    @After
    public void clear() {
        registry.remove(null);
    }
    @Test
    public void testRegister() {
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setArray(true).build();

        ApiDefinition apiDefinition = ApiDefinitionImpl.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();

        registry.add(apiDefinition);
        Assert.assertEquals(1, registry.getDefinitions().size());

        apiDefinition = ApiDefinitionImpl.builder().setName("get_device2")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();

        registry.add(apiDefinition);
        Assert.assertEquals(2, registry.getDefinitions().size());
    }

    @Test
    public void testUniqueName() {
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setArray(true).build();

        ApiDefinition apiDefinition = ApiDefinitionImpl.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();

        registry.add(apiDefinition);
        registry.add(apiDefinition);
        Assert.assertEquals(1, registry.getDefinitions().size());
    }

    @Test
    public void testFilterByName() {
        HttpEndpoint httpEndpoint = HttpEndpoint.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setService("device")
                .setArray(true).build();

        ApiDefinition apiDefinition = ApiDefinitionImpl.builder().setName("get_device")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();

        registry.add(apiDefinition);
        Assert.assertEquals(1, registry.getDefinitions().size());

        apiDefinition = ApiDefinitionImpl.builder().setName("get_device2")
                .setMethod(HttpMethod.GET)
                .setPath("devices/")
                .setEndpoints(Lists.newArrayList(httpEndpoint))
                .build();

        registry.add(apiDefinition);
        Assert.assertEquals(2, registry.getDefinitions().size());

        List<ApiDefinition> definitions = registry.filter("get_device");
        Assert.assertNotNull(apiDefinition);
        Assert.assertEquals(1, definitions.size());
        Assert.assertEquals("get_device", definitions.get(0).name());

        definitions = registry.filter("get_device3");
        Assert.assertNotNull(apiDefinition);
        Assert.assertEquals(0, definitions.size());
    }
}
