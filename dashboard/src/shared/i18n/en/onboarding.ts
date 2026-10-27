import type { Dictionary } from "../types";

export const enOnboarding: Pick<Dictionary, "onboarding"> = {
  onboarding: {
    checking: "Checking your invitation…",
    title: "Welcome to Zella Charging",
    subtitle: "Create your password to activate your access to the console.",
    account: "Account",
    invalidTitle: "Invalid invitation",
    invalidText: "This link has expired or has already been used. Ask your administrator for a new invitation.",
    backToLogin: "Go to sign in",
    password: "Password",
    passwordHint: "At least 10 characters, with letters and digits.",
    confirm: "Confirm password",
    submit: "Create my password",
    submitting: "Creating…",
    completeTitle: "Complete your profile",
    completeSubtitle: "This information is required before you can access the console.",
    steps: { password: "Password", security: "Security", profile: "Profile" },
    stepLabel: "Step {current} of {total}",
  },
};
