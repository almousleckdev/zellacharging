"use client";

import { displayName, initialsOf, ownAvatarUrl } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Avatar } from "@/shared/ui/Avatar";
import { Badge } from "@/shared/ui/Badge";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";

export function AccountCard() {
  const { t } = useI18n();
  const { profile } = useSession();
  const roles: Record<string, string> = t.shell.roles;
  const labels = t.profile.account;

  return (
    <Card title={labels.title}>
      <div className="flex items-center gap-4">
        <Avatar initials={initialsOf(profile.firstName, profile.lastName, profile.email)} src={ownAvatarUrl(profile.avatarVersion)} size={56} className="text-lg" />
        <div className="min-w-0">
          <p className="truncate text-base font-semibold text-foreground">{displayName(profile.firstName, profile.lastName, profile.email)}</p>
          <p className="text-sm text-muted">{roles[profile.role] ?? profile.role}</p>
        </div>
      </div>
      <div className="mt-5 border-t border-border pt-5">
        <FactList
          facts={[
            { label: labels.email, value: <span className="block truncate">{profile.email}</span> },
            { label: labels.role, value: roles[profile.role] ?? profile.role },
            { label: labels.status, value: <Badge tone="success">{labels.active}</Badge> },
          ]}
        />
      </div>
    </Card>
  );
}
