package com.edgar.direwolves.definition;

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
public class RateLimitDefinitionRegistryTest {
    RateLimitDefinitionRegistry registry;
    @Before
    public void setUp() {
        registry = RateLimitDefinitionRegistry.create();
        registry.add(RateLimitDefinition.create("get_device", RateLimitBy.USER, RateLimitType.SECOND, 100));
        registry.add(RateLimitDefinition.create("get_device", RateLimitBy.USER, RateLimitType.DAY, 10000));
    }

    @After
    public void clear() {
        registry.remove(null, null, null);
    }

    @Test
    public void testRegister() {
        Assert.assertEquals(2, registry.getDefinitions().size());
    }

    @Test
    public void testUnique() {
        registry.add(new RateLimitDefinitionImpl("get_device", RateLimitBy.USER, RateLimitType.DAY, 1000));
        Assert.assertEquals(2, registry.getDefinitions().size());
    }

    @Test
    public void testFilter() {
        List<RateLimitDefinition> definitions = registry.filter("get_device", RateLimitBy.USER, RateLimitType.DAY);
        Assert.assertNotNull(definitions);
        Assert.assertEquals(1, definitions.size());
        Assert.assertEquals(RateLimitBy.USER, definitions.get(0).rateLimitBy());

        definitions = registry.filter("get_device2", RateLimitBy.USER, RateLimitType.YEAR);
        Assert.assertNotNull(definitions);
        Assert.assertEquals(0, definitions.size());
    }

    @Test
    public void testRemove() {
        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove("get_device", RateLimitBy.USER, RateLimitType.DAY);

        Assert.assertEquals(1, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveByName() {
        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove("get_device", null, null);

        Assert.assertEquals(0, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveByType() {
        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove(null, null, RateLimitType.DAY);

        Assert.assertEquals(1, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveByBy() {
        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove(null, RateLimitBy.USER, null);

        Assert.assertEquals(0, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveAll() {

        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove(null, null, null);

        Assert.assertEquals(0, registry.getDefinitions().size());
    }
}
