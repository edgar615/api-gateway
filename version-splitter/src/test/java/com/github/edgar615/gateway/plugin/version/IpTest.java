package com.github.edgar615.gateway.plugin.version;

import com.github.edgar615.util.net.IPUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Edgar on 2018/4/3.
 *
 * @author Edgar  Date 2018/4/3
 */
public class IpTest {

    @Test
    public void testRange() {
        long start = IPUtils.ipToLong("192.168.1.2");
        long end = IPUtils.ipToLong("192.168.1.102");
        IpRangePolicy policy = new IpRangePolicy(start, end, "0.0.1");
        Assert.assertTrue(policy.satisfy("192.168.1.3"));
        Assert.assertFalse(policy.satisfy("192.168.1.103"));
    }

    @Test
    public void testHash() {
        System.out.println(IPUtils.ipToLong("172.168.1.101") % 100);
        System.out.println(IPUtils.ipToLong("172.168.1.103") % 100);
        System.out.println(IPUtils.ipToLong("192.168.1.2") % 100);
        System.out.println(IPUtils.ipToLong("192.169.1.102") % 100);
        IpHashPolicy policy = new IpHashPolicy(10, 15, "0.0.1");
        Assert.assertTrue(policy.satisfy("192.169.1.102"));
        Assert.assertFalse(policy.satisfy("192.168.1.2"));
    }

    @Test
    public void testAppoint() {
        IpAppointPolicy policy = new IpAppointPolicy("0.0.1");
        policy.addIp("192.168.*");
        Assert.assertTrue(policy.satisfy("192.168.1.102"));
        Assert.assertFalse(policy.satisfy("192.169.1.2"));
    }
}
