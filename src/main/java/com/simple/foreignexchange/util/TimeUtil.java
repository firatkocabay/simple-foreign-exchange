package com.simple.foreignexchange.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class TimeUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtil() {
    }

    public static Instant convertMissingMillisecondsToInstant(Long milliseconds) {
        String fullMilliseconds = milliseconds + "000";
        return new Date(Long.parseLong(fullMilliseconds)).toInstant();
    }

    public static String getFormattedInstantDate(Instant instantTime) {
        if (Objects.isNull(instantTime))
            return null;
        return DEFAULT_FORMATTER.withZone(ZoneId.systemDefault()).format(instantTime);
    }

}
