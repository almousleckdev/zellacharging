package com.evcharge.customer.account;

import java.util.List;
import java.util.UUID;

public interface CustomerSignIns {

    List<SignInMethod> of(UUID customerId);
}
