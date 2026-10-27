"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { useAdminLogin } from "../hooks/useAdminLogin";
import { CredentialsStep } from "./CredentialsStep";
import { TotpStep } from "./TotpStep";

export function LoginForm() {
  const { t } = useI18n();
  const login = useAdminLogin();
  const enrolling = login.step?.stage === "TOTP_ENROLLMENT";

  const heading = !login.step
    ? { title: t.login.title, subtitle: t.login.subtitle }
    : enrolling
      ? { title: t.totp.enrollTitle, subtitle: t.totp.enrollSubtitle }
      : { title: t.totp.requiredTitle, subtitle: t.totp.requiredSubtitle };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1.5">
        <h1 className="text-2xl font-semibold tracking-tight text-foreground">{heading.title}</h1>
        <p className="text-sm text-muted">{heading.subtitle}</p>
      </div>
      {login.errorCode && <Alert>{t.errors[login.errorCode]}</Alert>}
      {login.step ? (
        <TotpStep step={login.step} pending={login.pending} onSubmit={login.submitCode} onBack={login.restart} />
      ) : (
        <CredentialsStep pending={login.pending} onSubmit={login.submitCredentials} />
      )}
    </div>
  );
}
