"use client";

import { useEffect, useId, useRef } from "react";
import type { ReactNode } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Icon } from "./Icon";

type ModalProps = {
  open: boolean;
  title: string;
  description?: string;
  busy?: boolean;
  size?: "sm" | "lg";
  onClose: () => void;
  footer: ReactNode;
  children?: ReactNode;
};

export function Modal({ open, title, description, busy = false, size = "sm", onClose, footer, children }: ModalProps) {
  const { t } = useI18n();
  const dialog = useRef<HTMLDialogElement>(null);
  const titleId = useId();
  const descriptionId = useId();

  useEffect(() => {
    const element = dialog.current;
    if (!element) {
      return;
    }
    if (open && !element.open) {
      element.showModal();
    } else if (!open && element.open) {
      element.close();
    }
  }, [open]);

  function requestClose() {
    if (!busy) {
      onClose();
    }
  }

  return (
    <dialog
      ref={dialog}
      aria-labelledby={titleId}
      aria-describedby={description ? descriptionId : undefined}
      onCancel={(event) => {
        event.preventDefault();
        requestClose();
      }}
      onClick={(event) => {
        if (event.target === dialog.current) {
          requestClose();
        }
      }}
      className={cn(
        "m-auto max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] overflow-y-auto overflow-x-hidden rounded-xl",
        "bg-background p-0 text-foreground shadow-xl backdrop:bg-slate-900/50 backdrop:backdrop-blur-sm",
        size === "lg" ? "max-w-2xl" : "max-w-md",
        open && "flex flex-col",
      )}
    >
      <header className="flex items-start justify-between gap-4 px-6 pb-1 pt-6">
        <div className="flex min-w-0 flex-col gap-1.5">
          <h2 id={titleId} className="text-lg font-semibold tracking-tight">
            {title}
          </h2>
          {description && (
            <p id={descriptionId} className="break-words text-sm text-muted">
              {description}
            </p>
          )}
        </div>
        {size === "lg" && (
          <button
            type="button"
            onClick={requestClose}
            aria-label={t.dialogs.close}
            className="inline-flex size-8 shrink-0 items-center justify-center rounded-full text-subtle hover:bg-surface-strong"
          >
            <Icon name="close" className="size-4" />
          </button>
        )}
      </header>
      {children && <div className="min-h-0 flex-1 overflow-y-auto px-6 py-5">{children}</div>}
      <footer className="flex flex-col-reverse gap-2 px-6 pb-6 pt-4 sm:flex-row sm:justify-end">
        {footer}
      </footer>
    </dialog>
  );
}
