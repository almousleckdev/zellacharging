"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Icon } from "@/shared/ui/Icon";
import { NAV_GROUPS } from "../navigation";
import type { NavItem } from "../navigation";

const ITEM = "flex h-9 items-center gap-3 rounded-control px-3 text-sm";

function AvailableItem({ item, active, label, onNavigate }: ItemProps) {
  const { scopedHref } = useSession();

  return (
    <Link
      href={scopedHref(item.href)}
      onClick={onNavigate}
      aria-current={active ? "page" : undefined}
      className={cn(
        ITEM,
        "font-medium transition-colors",
        active ? "bg-primary-soft text-foreground" : "text-muted hover:bg-surface-strong hover:text-foreground",
      )}
    >
      <Icon name={item.icon} className={cn("size-[18px]", active && "text-primary-text")} />
      {label}
    </Link>
  );
}

function PendingItem({ item, label, badge }: ItemProps & { badge: string }) {
  return (
    <span aria-disabled="true" className={cn(ITEM, "cursor-not-allowed text-subtle")}>
      <Icon name={item.icon} className="size-[18px]" />
      <span className="flex-1 truncate">{label}</span>
      <span className="rounded-full bg-surface-strong px-2 py-0.5 text-[0.6875rem] font-medium text-subtle">{badge}</span>
    </span>
  );
}

type ItemProps = {
  item: NavItem;
  active: boolean;
  label: string;
  onNavigate?: () => void;
};

export function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  const { t } = useI18n();
  const pathname = usePathname();
  const { profile } = useSession();

  return (
    <nav aria-label={t.shell.mainNavigation} className="flex flex-col gap-6">
      {NAV_GROUPS.map((group) => (
        <div key={group.id} className="flex flex-col gap-1">
          <p className="px-3 pb-1 text-xs font-semibold uppercase tracking-wide text-subtle">{t.nav.groups[group.id]}</p>
          {group.items.filter((item) => !item.permission || profile.permissions.includes(item.permission)).map((item) => {
            const props = { item, active: item.href === "/" ? pathname === "/" : pathname.startsWith(item.href), label: t.nav[item.id], onNavigate };
            return item.available ? (
              <AvailableItem key={item.id} {...props} />
            ) : (
              <PendingItem key={item.id} {...props} badge={t.shell.comingSoon} />
            );
          })}
        </div>
      ))}
    </nav>
  );
}
