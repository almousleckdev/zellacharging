"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "@/shared/ui/Icon";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";
import { CountrySwitcher } from "./CountrySwitcher";

export function Topbar({ onOpenMenu }: { onOpenMenu: () => void }) {
  const { t } = useI18n();

  return (
    <div className="sticky top-0 z-20 flex h-14 items-center justify-between gap-3 border-b border-border bg-background px-4 sm:px-6">
      <button
        type="button"
        onClick={onOpenMenu}
        aria-label={t.shell.openMenu}
        className="inline-flex size-9 items-center justify-center rounded-control text-muted hover:bg-surface-strong lg:hidden"
      >
        <Icon name="menu" />
      </button>
      <div className="ml-auto flex items-center gap-3">
        <CountrySwitcher />
        <LanguageSwitch />
      </div>
    </div>
  );
}
