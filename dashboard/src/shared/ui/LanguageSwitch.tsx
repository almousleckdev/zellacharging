"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import type { Locale } from "@/shared/i18n/types";
import { cn } from "@/shared/lib/cn";

const LOCALES: Locale[] = ["fr", "en"];

export function LanguageSwitch() {
  const { locale, setLocale, t } = useI18n();

  return (
    <div role="group" aria-label={t.language.label} className="inline-flex rounded-control border border-border p-0.5">
      {LOCALES.map((option) => (
        <button
          key={option}
          type="button"
          aria-pressed={locale === option}
          onClick={() => setLocale(option)}
          className={cn(
            "h-7 min-w-9 rounded-[0.375rem] px-2 text-xs font-semibold transition-colors",
            locale === option ? "bg-surface-strong text-foreground" : "text-subtle hover:text-foreground",
          )}
        >
          {t.language[option]}
        </button>
      ))}
    </div>
  );
}
