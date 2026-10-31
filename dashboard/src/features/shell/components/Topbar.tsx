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
    <div className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-border bg-canvas/90 px-4 backdrop-blur sm:px-6 lg:px-10">
      <button
        type="button"
        onClick={onOpenMenu}
        aria-label={t.shell.openMenu}
        className="inline-flex size-9 items-center justify-center rounded-control text-muted hover:bg-surface-strong lg:hidden"
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
