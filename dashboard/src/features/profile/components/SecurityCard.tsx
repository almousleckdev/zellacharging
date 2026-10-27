"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import { Card } from "@/shared/ui/Card";
import { ChangePasswordForm } from "./ChangePasswordForm";

export function SecurityCard() {
  const { t } = useI18n();
  const labels = t.profile.security;

  return (
    <Card title={labels.title}>
      <div className="flex flex-col gap-6">
        <div className="flex items-start justify-between gap-4">
          <div className="flex flex-col gap-0.5">
            <p className="text-sm font-medium text-foreground">{labels.twoFactor}</p>
            <p className="text-sm text-muted">{labels.twoFactorHint}</p>
          </div>
          <Badge tone="success">{labels.enabled}</Badge>
        </div>
        <div className="flex flex-col gap-4 border-t border-border pt-6">
          <h3 className="text-sm font-semibold text-foreground">{t.profile.password.title}</h3>
          <ChangePasswordForm />
        </div>
      </div>
    </Card>
  );
}
