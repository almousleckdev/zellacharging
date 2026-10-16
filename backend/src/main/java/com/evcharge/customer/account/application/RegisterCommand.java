package com.evcharge.customer.account.application;

public record RegisterCommand(String firstName, String lastName, String email, String phone, String password) {
}
