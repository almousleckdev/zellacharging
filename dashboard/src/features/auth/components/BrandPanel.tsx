"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { ChargeBar } from "@/shared/ui/ChargeBar";

const LEVELS = [0.92, 0.64, 0.38, 0.78];

export function BrandPanel() {
  const { t } = useI18n();

  return (
    <aside className="relative hidden flex-col justify-between overflow-hidden bg-ink p-12 text-ink-text lg:flex xl:p-16">
      <div className="flex flex-col gap-3" aria-hidden="true">
        {LEVELS.map((level, index) => (
          <ChargeBar key={index} level={level} cells={28} tone="onDark" className="h-4 opacity-90" />
        ))}
      </div>
      <div className="flex max-w-lg flex-col gap-10">
        <h2 className="font-display text-4xl font-semibold leading-[1.1] text-white xl:text-5xl">{t.panel.title}</h2>
        <ul className="flex flex-col gap-5">
          {t.panel.items.map((item) => (
            <li key={item.title} className="flex flex-col gap-1 border-l-2 border-primary pl-4">
              <p className="text-sm font-semibold text-white">{item.title}</p>
              <p className="text-sm text-ink-muted">{item.text}</p>
            </li>
          ))}
        </ul>
      </div>
      <p className="flex max-w-lg items-center gap-2 text-xs text-ink-muted">
        <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="size-4 shrink-0">
          <rect x="5" y="11" width="14" height="9" rx="2" />
          <path d="M8 11V8a4 4 0 0 1 8 0v3" strokeLinecap="round" />
        </svg>
        {t.panel.security}
      </p>
    </aside>
  );
}
