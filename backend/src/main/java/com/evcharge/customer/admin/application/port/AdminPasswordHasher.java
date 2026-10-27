package com.evcharge.customer.admin.application.port;

public interface AdminPasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hash);
}
