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
public class AuthDefinitionRegistryTest {
    AuthDefinitionRegistry registry;
    @Before
    public void setUp() {
        registry = AuthDefinitionRegistry.create();
        registry.add(new AuthDefinitionImpl("get_device", AuthType.JWT));
        registry.add(new AuthDefinitionImpl("get_device", AuthType.APP_KEY));
    }

    @After
    public void clear() {
        registry.remove(null, null);
    }

    @Test
    public void testRegister() {
        Assert.assertEquals(2, registry.getDefinitions().size());
    }

    @Test
    public void testUnique() {
        registry.add(new AuthDefinitionImpl("get_device", AuthType.JWT));
        Assert.assertEquals(2, registry.getDefinitions().size());
    }

    @Test
    public void testFilter() {
        List<AuthDefinition> definitions = registry.filter("get_device", AuthType.JWT);
        Assert.assertNotNull(definitions);
        Assert.assertEquals(1, definitions.size());
        Assert.assertEquals(AuthType.JWT, definitions.get(0).authType());

        definitions = registry.filter("get_device2", AuthType.JWT);
        Assert.assertNotNull(definitions);
        Assert.assertEquals(0, definitions.size());
    }

    @Test
    public void testRemove() {
        Assert.assertEquals(2, registry.getDefinitions().size());

         registry.remove("get_device", AuthType.JWT);

        Assert.assertEquals(1, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveByName() {

        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove("get_device", null);

        Assert.assertEquals(0, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveByType() {

        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove(null, AuthType.JWT);

        Assert.assertEquals(1, registry.getDefinitions().size());
    }

    @Test
    public void testRemoveAll() {

        Assert.assertEquals(2, registry.getDefinitions().size());

        registry.remove(null, null);

        Assert.assertEquals(0, registry.getDefinitions().size());
    }
}
