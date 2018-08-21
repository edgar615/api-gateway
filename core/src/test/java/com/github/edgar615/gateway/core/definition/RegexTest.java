package com.github.edgar615.gateway.core.definition;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Edgar on 2017/11/15.
 *
 * @author Edgar  Date 2017/11/15
 */
public class RegexTest {

    @Test
    public void testRegex() {
        Pattern pattern = Pattern.compile("/devices/(\\d+)");
        Matcher matcher = pattern.matcher("/devices/133");
        boolean result = matcher.matches();
        Assert.assertTrue(result);
        displayParam(matcher);

        matcher = pattern.matcher("/devices/dd");
        result = matcher.matches();
        Assert.assertFalse(result);
        displayParam(matcher);

        pattern = Pattern.compile("/devices/(\\d+)/(\\w+)");
        matcher = pattern.matcher("/devices/123/dd");
        result = matcher.matches();
        Assert.assertTrue(result);
        displayParam(matcher);

        matcher = pattern.matcher("/devices/dd/123");
        result = matcher.matches();
        Assert.assertFalse(result);
        displayParam(matcher);

        pattern = Pattern.compile("/devices/(\\S+)");
        matcher = pattern.matcher("/devices/d-d");
        result = matcher.matches();
        Assert.assertTrue(result);
        displayParam(matcher);

        matcher = pattern.matcher("/devices/d-d/123/dercz5454");
        result = matcher.matches();
        Assert.assertTrue(result);
        displayParam(matcher);

        pattern = Pattern.compile("/devices/(\\S+)/(\\d+)");
        matcher = pattern.matcher("/devices/d-d/3445");
        result = matcher.matches();
        Assert.assertTrue(result);
        displayParam(matcher);

        matcher = pattern.matcher("/devices/134/d-d/3445");
        result = matcher.matches();
        Assert.assertTrue(result);
        displayParam(matcher);

        pattern = Pattern.compile("\\{[^/]+?\\}");
        matcher = pattern.matcher("/devices/134/d-d/3445");
        result = matcher.matches();
        System.out.println(result);
        displayParam(matcher);

    }

    private void displayParam(Matcher matcher) {
        if (matcher.matches()) {
            try {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String group = matcher.group(i + 1);
                    if (group != null) {
                        final String k = "param" + i;
                        final String value = URLDecoder.decode(group, "UTF-8");
                        System.out.println(value);
                    }
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
    }
}
