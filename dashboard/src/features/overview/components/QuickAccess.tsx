"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { Icon } from "@/shared/ui/Icon";
import type { IconName } from "@/shared/ui/Icon";

type Shortcut = { id: "customers" | "payments" | "stations" | "reports"; href: string; icon: IconName; permission: string };

const SHORTCUTS: Shortcut[] = [
  { id: "customers", href: "/customers", icon: "users", permission: "CUSTOMERS_READ" },
  { id: "payments", href: "/payments", icon: "wallet", permission: "PAYMENTS_READ" },
  { id: "stations", href: "/stations", icon: "station", permission: "STATIONS_READ" },
  { id: "reports", href: "/reports", icon: "chart", permission: "PAYMENTS_READ" },
];

export function QuickAccess() {
  const { t } = useI18n();
  const { profile, scopedHref } = useSession();
  const visible = SHORTCUTS.filter((item) => profile.permissions.includes(item.permission));

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
              className="flex flex-col items-center gap-1.5 rounded-control bg-surface p-2 text-center text-xs font-medium text-foreground transition-colors hover:bg-surface-strong"
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
