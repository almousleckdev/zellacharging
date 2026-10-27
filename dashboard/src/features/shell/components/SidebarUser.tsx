"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { displayName, initialsOf, ownAvatarUrl } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { Avatar } from "@/shared/ui/Avatar";
import { cn } from "@/shared/lib/cn";
import { LogoutControl } from "./LogoutControl";

type SidebarUserProps = {
  onNavigate?: () => void;
};

export function SidebarUser({ onNavigate }: SidebarUserProps) {
  const { t } = useI18n();
  const { profile, scopedHref } = useSession();
  const pathname = usePathname();
  const roles: Record<string, string> = t.shell.roles;
  const active = pathname === "/profile";

  return (
    <div className="flex items-center gap-1 border-t border-border pt-4">
      <Link
        href={scopedHref("/profile")}
        onClick={onNavigate}
        aria-label={t.shell.profileLink}
        aria-current={active ? "page" : undefined}
        title={t.shell.profileLink}
        className={cn(
          "flex min-w-0 flex-1 items-center gap-3 rounded-control p-1.5 transition-colors",
          active ? "bg-primary-soft" : "hover:bg-surface-strong",
        )}
      >
        <Avatar initials={initialsOf(profile.firstName, profile.lastName, profile.email)} src={ownAvatarUrl(profile.avatarVersion)} />
        <span className="min-w-0 flex-1">
          <span className="block truncate text-sm font-medium text-foreground">
            {displayName(profile.firstName, profile.lastName, profile.email)}
          </span>
          <span className="block truncate text-xs text-subtle">{roles[profile.role] ?? profile.role}</span>
        </span>
      </Link>
      <LogoutControl />
    </div>
  );
}
