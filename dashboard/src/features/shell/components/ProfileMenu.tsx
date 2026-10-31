"use client";

import Link from "next/link";
import { useId, useRef, useState } from "react";
import { displayName, initialsOf, ownAvatarUrl } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useDismiss } from "@/shared/lib/useDismiss";
import { Avatar } from "@/shared/ui/Avatar";
import { Icon } from "@/shared/ui/Icon";
import { LogoutDialog } from "./LogoutDialog";

const ITEM = "flex w-full items-center gap-2.5 px-4 py-2.5 text-left text-sm hover:bg-surface-strong";

export function ProfileMenu() {
  const { t } = useI18n();
  const { profile, scopedHref } = useSession();
  const [open, setOpen] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const menuId = useId();
  const roles: Record<string, string> = t.shell.roles;
  const name = displayName(profile.firstName, profile.lastName, profile.email);

  useDismiss(open, root, () => setOpen(false));

  return (
    <div ref={root} className="relative">
      <button
        type="button"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
        aria-label={t.shell.accountMenu}
        onClick={() => setOpen((current) => !current)}
        className="flex items-center gap-3 rounded-control p-1 pr-2 hover:bg-surface-strong"
      >
        <Avatar initials={initialsOf(profile.firstName, profile.lastName, profile.email)} src={ownAvatarUrl(profile.avatarVersion)} size={36} />
        <span className="hidden min-w-0 flex-col text-left md:flex">
          <span className="max-w-40 truncate text-sm font-semibold text-foreground">{name}</span>
          <span className="text-xs text-subtle">{roles[profile.role] ?? profile.role}</span>
        </span>
        <Icon name="chevronDown" className="hidden size-4 text-subtle md:block" />
      </button>
      {open && (
        <div id={menuId} role="menu" className="absolute right-0 z-40 mt-2 w-64 overflow-hidden rounded-card border border-border bg-background shadow-lg">
          <div className="flex flex-col gap-0.5 border-b border-border px-4 py-3">
            <p className="truncate text-sm font-semibold text-foreground">{name}</p>
            <p className="truncate text-xs text-subtle">{profile.email}</p>
          </div>
          <Link role="menuitem" href={scopedHref("/profile")} onClick={() => setOpen(false)} className={ITEM}>
            <Icon name="user" className="size-4 text-subtle" />
            {t.shell.profileLink}
          </Link>
          <Link role="menuitem" href={scopedHref("/profile#preferences")} onClick={() => setOpen(false)} className={ITEM}>
            <Icon name="sliders" className="size-4 text-subtle" />
            {t.shell.settings}
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={() => {
              setOpen(false);
              setConfirming(true);
            }}
            className={`${ITEM} border-t border-border text-danger`}
          >
            <Icon name="logout" className="size-4" />
            {t.shell.logout}
          </button>
        </div>
      )}
      <LogoutDialog open={confirming} onClose={() => setConfirming(false)} />
    </div>
  );
}
