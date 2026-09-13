package com.fawry.routing.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public final class AvailabilityChecker {

    private AvailabilityChecker() {
    }

    public static boolean isAvailableAt(DayOfWeek dayFrom,
                                        DayOfWeek dayTo,
                                        LocalTime from,
                                        LocalTime to,
                                        ZonedDateTime now) {
        if (now == null) {
            return false;
        }
        return isWithinWindow(from, to, now.toLocalTime())
                && isWithinDayRange(dayFrom, dayTo, now.getDayOfWeek());
    }


    public static boolean isWithinWindow(LocalTime from, LocalTime to, LocalTime now) {
        if (from == null || to == null || now == null) {
            return false;
        }
        if (from.equals(to)) {
            return true;
        }
        if (from.isBefore(to)) {
            return !now.isBefore(from) && now.isBefore(to);
        }
        // Crosses midnight.
        return !now.isBefore(from) || now.isBefore(to);
    }


    public static boolean isWithinDayRange(DayOfWeek from, DayOfWeek to, DayOfWeek day) {
        if (from == null || to == null || day == null) {
            return false;
        }
        // How many days the range spans, and how far into it the candidate day sits.
        var span = Math.floorMod(to.getValue() - from.getValue(), 7);
        var offset = Math.floorMod(day.getValue() - from.getValue(), 7);
        return offset <= span;
    }
}
