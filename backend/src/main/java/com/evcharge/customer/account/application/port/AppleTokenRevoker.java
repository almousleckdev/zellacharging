package com.evcharge.customer.account.application.port;

public interface AppleTokenRevoker {

    void revoke(String authorizationCode);
}
