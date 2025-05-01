package org.example.tablenow.global.constant;

import java.time.ZoneId;

public final class TimeConstants {
    private TimeConstants() {}

    public static final String TIME_ZONE_ASIA_SEOUL = "Asia/Seoul";
    public static final ZoneId ZONE_ID_ASIA_SEOUL = ZoneId.of(TIME_ZONE_ASIA_SEOUL);

    public static final String TIME_HH_MM = "HH:mm";
    public static final String TIME_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
}
