"use client";

import Link from "next/link";
import { useEffect, useId, useRef, useState } from "react";
import { displayName, initialsOf, ownAvatarUrl } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
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

  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (event: MouseEvent | KeyboardEvent) => {
      if (event instanceof KeyboardEvent ? event.key === "Escape" : !root.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", close);
    document.addEventListener("keydown", close);
    return () => {
      document.removeEventListener("mousedown", close);
      document.removeEventListener("keydown", close);
    };
  }, [open]);

  return (
    <div ref={root} className="relative">
      <button
        type="button"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
        aria-label={t.shell.accountMenu}
        onClick={() => setOpen((current) => !current)}
        className="flex size-10 items-center justify-center rounded-full ring-1 ring-border hover:ring-2 hover:ring-primary"
      >
        <Avatar initials={initialsOf(profile.firstName, profile.lastName, profile.email)} src={ownAvatarUrl(profile.avatarVersion)} size={36} />
      </button>
      {open && (
        <div id={menuId} role="menu" className="absolute right-0 z-40 mt-2 w-64 overflow-hidden rounded-card border border-border bg-background shadow-lg">
          <div className="flex flex-col gap-0.5 border-b border-border px-4 py-3">
            <p className="truncate text-sm font-semibold text-foreground">{name}</p>
            <p className="truncate text-xs text-subtle">{profile.email}</p>
            <p className="text-xs font-medium text-primary-text">{roles[profile.role] ?? profile.role}</p>
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
