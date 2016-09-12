package com.edgar.direwolves.definition;

public enum RateLimitType {

    /**
     * 秒
     */
    SECODE("second"),

    /**
     * 分钟
     */
    MINUTE("minute"),

    /**
     * 小时
     */
    HOUR("hour"),

    /**
     * 天
     */
    DAY("day"),

    /**
     * 月
     */
    MONTH("month"),
    /**
     * 年
     */
    YEAR("year");

    private final String value;

    RateLimitType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}