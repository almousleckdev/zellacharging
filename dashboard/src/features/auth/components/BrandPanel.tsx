"use client";

import type { ReactNode } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";

function Icon({ children }: { children: ReactNode }) {
  return (
    <span className="flex size-10 shrink-0 items-center justify-center rounded-control border border-border bg-background text-primary-text">
      <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="size-5">
        {children}
      </svg>
    </span>
  );
}

const ICONS: ReactNode[] = [
  <path key="pulse" d="M3 12h4l3-8 4 16 3-8h4" strokeLinecap="round" strokeLinejoin="round" />,
  <g key="globe">
    <circle cx="12" cy="12" r="9" />
    <path d="M3 12h18M12 3c2.6 2.6 3.9 5.6 3.9 9s-1.3 6.4-3.9 9c-2.6-2.6-3.9-5.6-3.9-9S9.4 5.6 12 3Z" />
  </g>,
  <g key="wallet">
    <rect x="3" y="6" width="18" height="13" rx="2" />
    <path d="M3 10h18M16 14.5h2" strokeLinecap="round" />
  </g>,
];

export function BrandPanel() {
  const { t } = useI18n();

  return (
    <aside className="relative hidden flex-col justify-between border-l border-border bg-surface p-12 lg:flex">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 opacity-70 [background-image:radial-gradient(var(--color-border-strong)_1px,transparent_1px)] [background-size:24px_24px]"
      />
      <div className="relative flex max-w-md flex-col gap-10">
        <div className="flex flex-col gap-4">
          <span className="h-1 w-10 rounded-full bg-primary" />
          <h2 className="text-3xl font-semibold leading-tight tracking-tight text-foreground">{t.panel.title}</h2>
        </div>
        <ul className="flex flex-col gap-6">
          {t.panel.items.map((item, index) => (
            <li key={item.title} className="flex items-start gap-4">
              <Icon>{ICONS[index]}</Icon>
              <div className="flex flex-col gap-1">
                <p className="text-sm font-semibold text-foreground">{item.title}</p>
                <p className="text-sm text-muted">{item.text}</p>
              </div>
            </li>
          ))}
        </ul>
      </div>
      <p className="relative flex max-w-md items-center gap-2 text-xs text-subtle">
        <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="size-4 shrink-0">
          <rect x="5" y="11" width="14" height="9" rx="2" />
          <path d="M8 11V8a4 4 0 0 1 8 0v3" strokeLinecap="round" />
        </svg>
        {t.panel.security}
      </p>
    </aside>
  );
}
