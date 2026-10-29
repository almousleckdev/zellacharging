package com.evcharge.customer.account.application;

import com.evcharge.customer.account.domain.Customer;

public record ProfileView(Customer customer, boolean emailVerified, boolean phoneVerified) {
}
