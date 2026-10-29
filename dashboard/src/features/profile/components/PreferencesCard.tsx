"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";

export function PreferencesCard() {
  const { t } = useI18n();

  return (
    <div id="preferences" className="scroll-mt-24">
      <Card title={t.profile.preferences.title}>
        <div className="flex items-center justify-between gap-3">
          <span className="text-sm text-muted">{t.profile.preferences.language}</span>
          <LanguageSwitch />
        </div>
      </Card>
    </div>
  );
}
