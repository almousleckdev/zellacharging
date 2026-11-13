package com.evcharge.customer.account.application;

import com.evcharge.customer.account.domain.CodeDelivery;

public record RegisterCommand(String firstName, String lastName, String email, String phone, String password,
                              CodeDelivery delivery) {
}
