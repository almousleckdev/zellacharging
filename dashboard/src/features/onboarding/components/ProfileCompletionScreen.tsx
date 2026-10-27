"use client";

import { AuthLayout } from "@/features/auth/components/AuthLayout";
import { AvatarUploader } from "@/features/profile/components/AvatarUploader";
import { ProfileDetailsForm } from "@/features/profile/components/ProfileDetailsForm";
import type { AdminProfile } from "@/features/session/schemas";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Stepper } from "@/shared/ui/Stepper";

export function ProfileCompletionScreen({ profile }: { profile: AdminProfile }) {
  const { t } = useI18n();
  const labels = t.onboarding;

  return (
    <AuthLayout>
      <div className="flex flex-col gap-6">
        <Stepper steps={[labels.steps.password, labels.steps.security, labels.steps.profile]} current={2} label={labels.stepLabel} />
        <div className="flex flex-col gap-1.5">
          <h1 className="text-2xl font-semibold tracking-tight">{labels.completeTitle}</h1>
          <p className="text-sm text-muted">{labels.completeSubtitle}</p>
        </div>
        <AvatarUploader profile={profile} />
        <ProfileDetailsForm profile={profile} submitLabel={t.common.continue} />
      </div>
    </AuthLayout>
  );
}
