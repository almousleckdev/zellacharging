package com.evcharge.customer.admin.web;

import com.evcharge.customer.shared.FreeSundayWindow;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

record ChangeWindowRequest(@NotNull DayOfWeek day, @NotNull LocalTime from, LocalTime until, boolean endOfDay) {

    FreeSundayWindow window() {
        return new FreeSundayWindow(day.name(), from.toString(), endOfDay || until == null ? null : until.toString(), endOfDay);
    }
}
