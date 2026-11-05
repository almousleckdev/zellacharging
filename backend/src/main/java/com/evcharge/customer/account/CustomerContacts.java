package com.evcharge.customer.account;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CustomerContacts {

    List<CustomerMatch> activeByEmailOrPhone(Collection<String> emails, Collection<String> phonesE164);

    boolean activeExists(UUID customerId);
}
