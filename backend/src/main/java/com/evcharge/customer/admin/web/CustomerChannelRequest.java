package com.evcharge.customer.admin.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

record CustomerChannelRequest(@NotBlank @Pattern(regexp = "EMAIL|PHONE") String channel,
                              @Pattern(regexp = "EMAIL|WHATSAPP|SMS") String delivery) {
}
