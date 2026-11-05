package com.evcharge.customer.admin.web;

import com.evcharge.customer.partner.IssuedKey;

import java.util.UUID;

record IssuedKeyResponse(UUID id, String key, String partner, String country) {

    static IssuedKeyResponse of(IssuedKey issued, String partner, String country) {
        return new IssuedKeyResponse(issued.id(), issued.key(), partner, country);
    }
}
