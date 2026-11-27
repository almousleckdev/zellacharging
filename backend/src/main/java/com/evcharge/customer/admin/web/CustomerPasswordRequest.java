package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CustomerPasswordRequest(@NotBlank @Size(min = 8, max = 72) String password) {

    @Override
    public String toString() {
        return "CustomerPasswordRequest[password=hidden]";
    }
}
