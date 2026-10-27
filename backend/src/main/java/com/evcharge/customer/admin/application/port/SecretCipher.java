package com.evcharge.customer.admin.application.port;

public interface SecretCipher {

    String encrypt(String plain);
    String decrypt(String encrypted);
}
