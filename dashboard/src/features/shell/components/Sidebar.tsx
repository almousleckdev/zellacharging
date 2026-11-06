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
          "fixed inset-y-0 left-0 z-40 flex w-72 flex-col bg-sidebar text-sidebar-muted lg:w-64",
          "transition-[transform,visibility] duration-200 lg:sticky lg:top-0 lg:z-auto lg:h-screen lg:translate-x-0 lg:visible",
          open ? "visible translate-x-0" : "invisible -translate-x-full",
        )}
      >
        <div className="relative flex h-16 shrink-0 items-center justify-center border-b border-sidebar-line">
          <Logo height={28} />
          <button
            type="button"
            onClick={onClose}
            aria-label={t.shell.closeMenu}
            className="absolute right-3 top-1/2 inline-flex size-9 -translate-y-1/2 items-center justify-center rounded-control text-sidebar-muted hover:bg-sidebar-raised hover:text-sidebar-text lg:hidden"
          >
            <Icon name="close" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto px-3 py-2 [scrollbar-color:var(--color-sidebar-line)_transparent] [scrollbar-width:thin]">
          <SidebarNav onNavigate={onClose} />
        </div>
      </aside>
    </>
  );
}
