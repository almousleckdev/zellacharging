"use client";

import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Icon } from "./Icon";

type Tone = "success" | "error";
type Toast = { id: number; tone: Tone; message: string };

type ToastApi = {
  notify: (tone: Tone, message: string) => void;
};

const DISMISS_MS = 6000;
const ToastContext = createContext<ToastApi | null>(null);

const TONES: Record<Tone, string> = {
  success: "border-border bg-background text-foreground",
  error: "border-danger-border bg-danger-soft text-danger",
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const { t } = useI18n();
  const [toasts, setToasts] = useState<Toast[]>([]);

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const notify = useCallback(
    (tone: Tone, message: string) => {
      const id = Date.now() + Math.random();
      setToasts((current) => [...current, { id, tone, message }]);
      window.setTimeout(() => dismiss(id), DISMISS_MS);
    },
    [dismiss],
  );

  const api = useMemo(() => ({ notify }), [notify]);

  return (
    <ToastContext value={api}>
      {children}
      <div aria-live="polite" className="pointer-events-none fixed bottom-4 right-4 z-50 flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role={toast.tone === "error" ? "alert" : "status"}
            className={cn("pointer-events-auto flex items-start gap-2.5 rounded-control border px-3 py-2.5 text-sm shadow-md", TONES[toast.tone])}
          >
            <Icon name={toast.tone === "error" ? "alert" : "check"} className="mt-0.5 size-4" />
            <p className="flex-1">{toast.message}</p>
            <button
              type="button"
              onClick={() => dismiss(toast.id)}
              aria-label={t.dialogs.close}
              className="rounded-control p-0.5 hover:bg-surface-strong"
            >
              <Icon name="close" className="size-4" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext>
  );
}

export function useToast(): ToastApi {
  const value = useContext(ToastContext);
  if (!value) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return value;
}
