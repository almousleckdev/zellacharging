"use client";

import { useId, useRef, useState } from "react";
import { cn } from "@/shared/lib/cn";
import { useDismiss } from "@/shared/lib/useDismiss";
import { Icon } from "./Icon";

export type ActionItem = {
  label: string;
  onSelect: () => void;
  danger?: boolean;
};

export function ActionMenu({ label, items, trigger }: { label: string; items: ActionItem[]; trigger?: string }) {
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const menuId = useId();

  useDismiss(open, root, () => setOpen(false));

  return (
    <div ref={root} className="relative inline-block text-left">
      <button
        type="button"
        aria-label={label}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
        onClick={() => setOpen((current) => !current)}
        className={cn(
          "inline-flex items-center justify-center rounded-control",
          trigger
            ? "h-10 gap-2 border border-border-strong bg-background px-4 text-sm font-semibold text-foreground hover:bg-surface"
            : "size-8 text-subtle hover:bg-surface-strong hover:text-foreground",
        )}
      >
        {trigger ? (
          <>
            {trigger}
            <Icon name="chevronDown" className="size-4" />
          </>
        ) : (
          <Icon name="more" className="size-5" />
        )}
      </button>
      {open && (
        <ul
          id={menuId}
          role="menu"
          className="absolute right-0 z-30 mt-1 w-56 overflow-hidden rounded-control border border-border bg-background py-1 shadow-lg"
        >
          {items.map((item) => (
            <li key={item.label} role="none">
              <button
                type="button"
                role="menuitem"
                onClick={() => {
                  setOpen(false);
                  item.onSelect();
                }}
                className={cn(
                  "w-full px-3 py-2 text-left text-sm hover:bg-surface-strong",
                  item.danger ? "text-danger" : "text-foreground",
                )}
              >
                {item.label}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
