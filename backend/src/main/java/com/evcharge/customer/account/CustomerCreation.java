package com.evcharge.customer.account;

import java.util.UUID;

public interface CustomerCreation {

    UUID create(String firstName, String lastName, String email, String phone);
}
