import type { Dictionary } from "../types";

export const enAuth: Pick<Dictionary, "login" | "totp"> = {
  login: {
    title: "Sign in to the console",
    subtitle: "Access the Zella Charging platform administration.",
    email: "Email address",
    emailPlaceholder: "name@company.com",
    password: "Password",
    showPassword: "Show password",
    hidePassword: "Hide password",
    submit: "Sign in",
    submitting: "Signing in…",
  },
  totp: {
    requiredTitle: "Two-step verification",
    requiredSubtitle: "Enter the 6-digit code shown by your authenticator app.",
    enrollTitle: "Secure your account",
    enrollSubtitle: "Two-factor authentication is required to access the console. You only set it up once.",
    stepScan: "Scan this QR code with your authenticator app.",
    stepEnter: "Then enter the 6-digit code it generates.",
    qrLabel: "Two-factor authentication setup QR code",
    manualKey: "Or enter this key manually",
    copy: "Copy key",
    copied: "Key copied",
    code: "Verification code",
    verify: "Verify and continue",
    verifying: "Verifying…",
    back: "Back to sign in",
  },
};
