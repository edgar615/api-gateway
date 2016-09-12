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
public class IpRestrictionDefinitionRegistryTest {

    IpRestrictionDefinitionRegistry registry;
    @Before
    public void setUp() {
        registry = IpRestrictionDefinitionRegistry.instance();
    }

    @After
    public void clear() {
        registry.remove("get_device");
        registry.remove("get_device2");
    }

    @Test
    public void testRegister() {

        registry.addBlacklist("get_device", "192.168.1.100");
        Assert.assertEquals(1, registry.getDefinitions().size());
        registry.addBlacklist("get_device", "192.168.1.101");
        registry.addWhitelist("get_device2", "192.168.1.101");

        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.removeWhitelist("get_device2", "192.168.1.101");

        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove("get_device2");
        Assert.assertEquals(1, registry.getDefinitions().size());
    }

    @Test
    public void testAddRemove() {

        registry.addBlacklist("get_device", "192.168.1.100");
        Assert.assertEquals(1, registry.getDefinitions().size());
        registry.addBlacklist("get_device", "192.168.1.101");
        registry.addWhitelist("get_device", "192.168.1.101");

        IpRestrictionDefinition definition = registry.filter("get_device");

        Assert.assertEquals(1, definition.getBlacklist().size());
        Assert.assertEquals(1, definition.getWhitelist().size());

    }

}
