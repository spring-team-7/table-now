package org.example.tablenow.domain.settlement.batch.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class JobTimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static String getNowFormatted() {
        return ZonedDateTime.now(KST).format(FORMATTER);
    }
}
