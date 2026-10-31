"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { PageHeader } from "@/shared/ui/PageHeader";
import { AccountCard } from "./AccountCard";
import { AvatarUploader } from "./AvatarUploader";
import { CountriesCard } from "./CountriesCard";
import { PermissionsCard } from "./PermissionsCard";
import { PreferencesCard } from "./PreferencesCard";
import { ProfileDetailsForm } from "./ProfileDetailsForm";
import { SecurityCard } from "./SecurityCard";

export function ProfilePage() {
  const { t } = useI18n();
  const { profile } = useSession();

  return (
    <div className="flex flex-col gap-6">
      <PageHeader title={t.profile.title} subtitle={t.profile.subtitle} />
      <div className="grid items-start gap-6 lg:grid-cols-3">
        <div className="flex flex-col gap-6">
          <AccountCard />
          <Card title={t.profile.avatar.title}>
            <AvatarUploader profile={profile} />
          </Card>
          <PreferencesCard />
        </div>
        <div className="flex flex-col gap-6 lg:col-span-2">
          <Card title={t.profile.details.title} description={t.profile.details.subtitle}>
            <ProfileDetailsForm profile={profile} />
          </Card>
          <SecurityCard />
          <CountriesCard />
          <PermissionsCard />
        </div>
      </div>
    </div>
  );
}
