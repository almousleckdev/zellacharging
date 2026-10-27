"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { Icon } from "@/shared/ui/Icon";

export function PermissionsCard() {
  const { t } = useI18n();
  const { profile } = useSession();
  const names: Record<string, string> = t.profile.permissionLabels;

  return (
    <Card title={t.profile.permissions.title} description={t.profile.permissions.subtitle}>
      <ul className="grid gap-2.5 sm:grid-cols-2">
        {profile.permissions.map((permission) => (
          <li key={permission} className="flex items-center gap-2.5 text-sm text-foreground">
            <Icon name="check" className="size-4 text-success" />
            {names[permission] ?? permission}
          </li>
        ))}
      </ul>
    </Card>
  );
}
