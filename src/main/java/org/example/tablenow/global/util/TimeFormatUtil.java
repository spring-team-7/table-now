package org.example.tablenow.global.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class TimeFormatUtil {
    private static final Pattern ISO_UTC_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z$"
    );

    public static boolean isValidUtcIso8601(String input) {
        if (!ISO_UTC_PATTERN.matcher(input).matches()) {
            return false;
        }
        try {
            ZonedDateTime.parse(input);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
