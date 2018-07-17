package com.github.edgar615.gateway.core.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2018/1/11.
 *
 * @author Edgar  Date 2018/1/11
 */
public class AntPathMatcherTest {

  @Test
  public void testMatch1() {
    AntPathMatcher.Builder builder = new AntPathMatcher.Builder();
    AntPathMatcher matcher = builder.build();
    String pattern = "/trip/api/*x";
    Assert.assertTrue(matcher.isMatch(pattern, "/trip/api/x"));
    Assert.assertTrue(matcher.isMatch(pattern, "/trip/api/ax"));
    Assert.assertTrue(matcher.isMatch(pattern, "/trip/api/abx"));
    Assert.assertFalse(matcher.isMatch(pattern, "/trip/api/abc"));

    Assert.assertEquals("x", matcher.extractPathWithinPattern(pattern, "/trip/api/x"));
    Assert.assertEquals("ax", matcher.extractPathWithinPattern(pattern, "/trip/api/ax"));
    Assert.assertEquals("abx", matcher.extractPathWithinPattern(pattern, "/trip/api/abx"));

    Assert.assertTrue(matcher.isMatch("/trip/api/*", "/trip/api/"));
  }

  @Test
  public void testMatch2() {
    AntPathMatcher.Builder builder = new AntPathMatcher.Builder();
    AntPathMatcher matcher = builder.build();
    String pattern = "/trip/api/?x";
    Assert.assertFalse(matcher.isMatch(pattern, "/trip/api/x"));
    Assert.assertTrue(matcher.isMatch(pattern, "/trip/api/ax"));
    Assert.assertFalse(matcher.isMatch(pattern, "/trip/api/abx"));
    Assert.assertFalse(matcher.isMatch(pattern, "/trip/api/abc"));

    Assert.assertEquals("", matcher.extractPathWithinPattern("/trip/api/x", "/trip/api/x"));
    Assert.assertEquals("x", matcher.extractPathWithinPattern(pattern, "/trip/api/x"));
    Assert.assertEquals("ax", matcher.extractPathWithinPattern(pattern, "/trip/api/ax"));
    Assert.assertEquals("abx", matcher.extractPathWithinPattern(pattern, "/trip/api/abx"));
  }

  @Test
  public void testMatch3() {
    AntPathMatcher.Builder builder = new AntPathMatcher.Builder();
    AntPathMatcher matcher = builder.build();
    String pattern = "/**/alie";
    Assert.assertTrue(matcher.isMatch(pattern, "/trip/api/alie"));
    Assert.assertTrue(matcher.isMatch(pattern, "/trip/are/api/alie"));
    Assert.assertFalse(matcher.isMatch(pattern, "/trip/are/pi/abc"));

    System.out.println(matcher.extractPathWithinPattern("/docs/*/*.html", "/docs/cvs/commit"
                                                                            + ".html"));
    System.out.println(matcher.extractPathWithinPattern("/docs/**", "/docs/cvs/commit"
                                                                          + ".html"));
    System.out.println(matcher.extractPathWithinPattern("/docs/*/test/*.html",
                                                        "/docs/cvs/test/commit"
                                                                    + ".html"));
  }

}
