"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { TotpStep } from "@/features/auth/components/TotpStep";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { Alert } from "@/shared/ui/Alert";
import { AuthHeading } from "@/shared/ui/AuthHeading";
import { Spinner } from "@/shared/ui/Spinner";
import { Stepper } from "@/shared/ui/Stepper";
import { useInvitation } from "../hooks/useInvitation";
import { PasswordSetupStep } from "./PasswordSetupStep";

export function AcceptInvitePage() {
  const { t } = useI18n();
  const token = useSearchParams().get("token");
  const invitation = useInvitation(token);
  const labels = t.onboarding;
  const steps = [labels.steps.password, labels.steps.security, labels.steps.profile];

  if (invitation.phase === "checking") {
    return (
      <p role="status" className="flex items-center gap-2 text-sm text-muted">
        <Spinner />
        {labels.checking}
      </p>
    );
  }
  if (invitation.phase === "invalid") {
    return (
      <div className="flex flex-col gap-4">
        <AuthHeading title={labels.invalidTitle} subtitle={labels.invalidText} />
        <Link href="/login" className="text-sm font-semibold text-primary-text hover:underline">
          {labels.backToLogin}
        </Link>
      </div>
    );
  }
  const securityPhase = invitation.phase === "security" && invitation.step;

  return (
    <div className="flex flex-col gap-6">
      <Stepper steps={steps} current={securityPhase ? 1 : 0} label={labels.stepLabel} />
      <AuthHeading title={securityPhase ? t.totp.enrollTitle : labels.title} subtitle={securityPhase ? t.totp.enrollSubtitle : labels.subtitle}>
        {!securityPhase && invitation.email && (
          <p className="text-sm text-foreground">
            {labels.account} : <span className="font-semibold">{invitation.email}</span>
          </p>
        )}
      </AuthHeading>
      {invitation.errorCode && <Alert>{errorMessage(t, invitation.errorCode)}</Alert>}
      {securityPhase ? (
        <TotpStep step={invitation.step as NonNullable<typeof invitation.step>} pending={invitation.pending} onSubmit={invitation.submitCode} />
      ) : (
        <PasswordSetupStep pending={invitation.pending} onSubmit={(password) => void invitation.submitPassword(password)} />
      )}
    </div>
  );
}
