package com.evcharge.customer.account.application.port;

import com.evcharge.customer.account.domain.Avatar;

import java.util.Optional;

public interface ProfilePictureFetcher {
    Optional<Avatar> fetch(String url);
}
