package com.evcharge.customer.partner;

import java.util.UUID;

public record FreeDriverOption(UUID driverId, String customerId, String fullName, String phone, String email,
                               String fleetNumber, String plate, String status, boolean linked, boolean inProgram,
                               UUID agreementId, String agreementStatus) {
}
