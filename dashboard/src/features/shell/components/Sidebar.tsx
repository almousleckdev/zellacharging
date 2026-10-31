"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Icon } from "@/shared/ui/Icon";
import { Logo } from "@/shared/ui/Logo";
import { SidebarNav } from "./SidebarNav";

type SidebarProps = {
  open: boolean;
  onClose: () => void;
};

export function Sidebar({ open, onClose }: SidebarProps) {
  const { t } = useI18n();

  return (
    <>
      {open && <div aria-hidden="true" onClick={onClose} className="fixed inset-0 z-30 bg-foreground/40 lg:hidden" />}
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex w-64 flex-col gap-6 border-r border-sidebar-line bg-sidebar px-4 py-5 text-sidebar-text",
          "transition-[transform,visibility] duration-200 lg:sticky lg:top-0 lg:z-auto lg:h-screen lg:translate-x-0 lg:visible",
          open ? "visible translate-x-0" : "invisible -translate-x-full",
        )}
      >
        <div className="flex items-start justify-between px-2">
          <div className="flex flex-col gap-1">
            <Logo height={24} />
            <p className="text-xs text-sidebar-muted">{t.shell.product}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label={t.shell.closeMenu}
            className="inline-flex size-9 items-center justify-center rounded-control text-sidebar-muted hover:bg-sidebar-raised lg:hidden"
          >
            <Icon name="close" />
          </button>
        </div>
        <div className="-mr-2 flex-1 overflow-y-auto pr-2 [scrollbar-color:var(--color-sidebar-line)_transparent] [scrollbar-width:thin]">
          <SidebarNav onNavigate={onClose} />
        </div>
      </aside>
    </>
  );
}
