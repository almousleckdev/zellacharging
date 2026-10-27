package com.evcharge.customer.admin.application;

public record AdminLoginStep(Stage stage, String challengeToken, String secret, String provisioningUri) {

    public enum Stage {
        TOTP_REQUIRED,
        TOTP_ENROLLMENT
    }

    static AdminLoginStep required(String challengeToken) {
        return new AdminLoginStep(Stage.TOTP_REQUIRED, challengeToken, null, null);
    }

    static AdminLoginStep enrollment(String challengeToken, String secret, String provisioningUri) {
        return new AdminLoginStep(Stage.TOTP_ENROLLMENT, challengeToken, secret, provisioningUri);
    }
}
