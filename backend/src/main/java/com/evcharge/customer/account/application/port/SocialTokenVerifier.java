package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.SocialProfile;
import com.evcharge.customer.account.domain.SocialProvider;

public interface SocialTokenVerifier {

    SocialProfile verify(SocialProvider provider, String idToken);
}
