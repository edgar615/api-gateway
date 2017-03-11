package com.edgar.direwolves.core.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2017/1/6.
 *
 * @author Edgar  Date 2017/1/6
 */
public class MultimapUtilsTest {

  @Test
  public void testGetFirst() {
    Multimap<String, String> header = ArrayListMultimap.create();
    header.put("h1", "h1.1");
    header.put("h1", "h1.2");
    header.put("h2", "h2");
    Assert.assertEquals("h1.1", MultimapUtils.getFirst(header, "h1"));
    Assert.assertEquals("h2", MultimapUtils.getFirst(header, "h2"));
    Assert.assertNull(MultimapUtils.getFirst(header, "h3"));
  }
}
