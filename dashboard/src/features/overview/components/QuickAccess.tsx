"use client";

import Link from "next/link";
import { NAV_GROUPS } from "@/features/shell/navigation";
import type { NavId } from "@/features/shell/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { Icon } from "@/shared/ui/Icon";

const SHORTCUT_IDS: NavId[] = ["customers", "payments", "stations", "reports"];

const SHORTCUTS = NAV_GROUPS.flatMap((group) => group.items).filter((item) => SHORTCUT_IDS.includes(item.id));

export function QuickAccess() {
  const { t } = useI18n();
  const { profile, scopedHref } = useSession();
  const visible = SHORTCUTS.filter((item) => !item.permission || profile.permissions.includes(item.permission));

  if (visible.length === 0) {
    return null;
  }

  return (
    <Card title={t.overview.quick.title}>
      <ul className="grid grid-cols-4 gap-2">
        {visible.map((item) => (
          <li key={item.id}>
            <Link
              href={scopedHref(item.href)}
              className="flex flex-col items-center gap-1.5 rounded-control border border-border p-2 text-center text-xs font-medium text-foreground transition-colors hover:bg-surface"
            >
              <span className="flex size-8 items-center justify-center rounded-control bg-primary-soft text-primary-text">
                <Icon name={item.icon} className="size-4" />
              </span>
              {t.nav[item.id]}
            </Link>
          </li>
        ))}
      </ul>
    </Card>
  );
}
