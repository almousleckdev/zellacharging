"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Icon } from "@/shared/ui/Icon";
import { Logo } from "@/shared/ui/Logo";
import { SidebarNav } from "./SidebarNav";
import { SidebarUser } from "./SidebarUser";

type SidebarProps = {
  open: boolean;
  onClose: () => void;
};

export function Sidebar({ open, onClose }: SidebarProps) {
  const { t } = useI18n();

  return (
    <>
      {open && <div aria-hidden="true" onClick={onClose} className="fixed inset-0 z-30 bg-ink/60 lg:hidden" />}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex w-64 flex-col gap-7 bg-ink p-5 text-ink-text",
          "transition-[transform,visibility] duration-200 lg:sticky lg:top-0 lg:z-auto lg:h-screen lg:translate-x-0 lg:visible",
          open ? "visible translate-x-0" : "invisible -translate-x-full",
        )}
      >
        <div className="flex items-center justify-between px-2 pt-1">
          <Logo height={22} />
          <button
            type="button"
            onClick={onClose}
            aria-label={t.shell.closeMenu}
            className="inline-flex size-9 items-center justify-center rounded-control text-ink-muted hover:bg-ink-raised lg:hidden"
          >
            <Icon name="close" />
          </button>
        </div>
        <div className="scroll-dark -mr-2 flex-1 overflow-y-auto pr-2">
          <SidebarNav onNavigate={onClose} />
        </div>
        <SidebarUser onNavigate={onClose} />
      </aside>
    </>
  );
}
