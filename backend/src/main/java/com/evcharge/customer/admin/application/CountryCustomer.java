package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerRecord;

public record CountryCustomer(String country, CustomerRecord customer) {
}
