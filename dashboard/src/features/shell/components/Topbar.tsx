"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "@/shared/ui/Icon";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";
import { CountrySwitcher } from "./CountrySwitcher";
import { NotificationBell } from "./NotificationBell";
import { ProfileMenu } from "./ProfileMenu";

export function Topbar({ onOpenMenu }: { onOpenMenu: () => void }) {
  const { t } = useI18n();

  return (
    <div className="sticky top-0 z-20 flex h-16 shrink-0 items-center gap-3 border-b border-border bg-background px-4 sm:px-6">
      <button
        type="button"
        onClick={onOpenMenu}
        aria-label={t.shell.openMenu}
        className="-ml-1 inline-flex size-9 items-center justify-center rounded-control text-muted hover:bg-surface-strong hover:text-foreground lg:hidden"
      >
        <Icon name="menu" />
      </button>
      <CountrySwitcher />
      <div className="ml-auto flex items-center gap-3">
        <LanguageSwitch />
        <NotificationBell />
        <ProfileMenu />
      </div>
    </div>
  );
}
