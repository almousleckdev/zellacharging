"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Icon } from "@/shared/ui/Icon";
import { NAV_GROUPS } from "../navigation";

export function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  const { t } = useI18n();
  const pathname = usePathname();
  const { profile, scopedHref } = useSession();

  return (
    <nav aria-label={t.shell.mainNavigation} className="flex flex-col">
      {NAV_GROUPS.map((group) => (
        <div key={group.id} className="flex flex-col gap-0.5 pb-1">
          <p className="px-3 pb-1 pt-4 text-xs font-medium text-sidebar-faint">{t.nav.groups[group.id]}</p>
          {group.items
            .filter((item) => !item.permission || profile.permissions.includes(item.permission))
            .map((item) => {
              const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
              return (
                <Link
                  key={item.id}
                  href={scopedHref(item.href)}
                  onClick={onNavigate}
                  aria-current={active ? "page" : undefined}
                  ref={active ? (node) => node?.scrollIntoView?.({ block: "nearest" }) : undefined}
                  className={cn(
                    "group relative flex h-10 items-center gap-3 rounded-control px-3 text-sm font-medium transition-colors",
                    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                    active ? "bg-sidebar-raised text-sidebar-text" : "text-sidebar-muted hover:bg-sidebar-raised hover:text-sidebar-text",
                  )}
                >
                  {active && <span aria-hidden="true" className="absolute inset-y-2 left-0 w-[3px] rounded-r bg-primary" />}
                  <Icon name={item.icon} className={cn("size-[18px]", active ? "text-primary" : "text-sidebar-faint group-hover:text-sidebar-muted")} />
                  {t.nav[item.id]}
                </Link>
              );
            })}
        </div>
      ))}
    </nav>
  );
}
