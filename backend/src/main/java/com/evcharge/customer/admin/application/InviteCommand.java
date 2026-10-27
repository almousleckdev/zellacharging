package com.evcharge.customer.admin.application;

public record InviteCommand(String email, AccessCommand access) {
}
