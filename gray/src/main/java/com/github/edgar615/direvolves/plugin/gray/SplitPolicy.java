package com.github.edgar615.direvolves.plugin.gray;

/**
 * Created by Edgar on 2018/2/10.
 *
 * @author Edgar  Date 2018/2/10
 */
public enum  SplitPolicy {

  /**
   * ip范围
   */
  IP_RANGE,

  /**
   * ip hash，用于计算百分比
   */
  IP_HASH,

  /**
   * 特定IP
   */
  IP_APPOINT,

  /**
   * 用户范围
   */
  USER_RANGE,

  /**
   * 用户 hash，用于计算百分比
   */
  USER_HASH,

  /**
   * 特定用户
   */
  USER_APPOINT,

  /**
   * 设备 hash，用于计算百分比
   */
  DEVICE_HASH,

  /**
   * 特定设备
   */
  DEVICE_APPOINT;
}
