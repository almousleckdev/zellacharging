package com.evcharge.customer.partner;

import java.util.List;
import java.util.UUID;

public interface PartnerKeys {

    IssuedKey issue(String partner, String country, UUID actorId);

    void revoke(UUID keyId, UUID actorId);

    List<PartnerKeyInfo> list(String partner);
}
