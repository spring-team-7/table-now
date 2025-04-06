package org.example.tablenow.domain.store.util;

import org.example.tablenow.domain.store.entity.Store;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class StoreUtils {

    public static List<LocalTime> getStoreTimeSlots(Store store) {
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime startTime = store.getStartTime();
        LocalTime endTime = store.getEndTime();

        if (startTime == null || endTime == null) {
            return timeSlots;
        }

        LocalTime currentTime = startTime;
        while (currentTime.isBefore(endTime)) {
            timeSlots.add(currentTime);
            currentTime = currentTime.plusHours(1);
        }
        return timeSlots;
    }
}
