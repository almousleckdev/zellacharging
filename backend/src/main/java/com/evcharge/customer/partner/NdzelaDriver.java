package com.evcharge.customer.partner;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NdzelaDriver(Person driver, Contract contract, Car car) {

    public record Person(String firstName, String lastName, String phone, String email, String status) {
    }

    public record Contract(String number, String status, LocalDate startDate, LocalDate endDate,
                           BigDecimal dailyRent) {
    }

    public record Car(String fleetNumber, String plate, String brand, String model, int year, String color) {
    }
}
