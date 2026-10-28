"use client";

import Image from "next/image";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ChargeBar } from "@/shared/ui/ChargeBar";
import { Logo } from "@/shared/ui/Logo";

export function BrandPanel() {
  const { t } = useI18n();

  return (
    <aside className="relative hidden flex-col justify-end overflow-hidden bg-ink p-12 text-ink-text lg:flex xl:p-16">
      <Image
        src="/images/auth_background.jpg"
        alt=""
        fill
        priority
        sizes="(min-width: 1024px) 50vw, 0px"
        className="object-cover object-[50%_55%]"
      />
      <div aria-hidden="true" className="absolute inset-0 bg-gradient-to-t from-ink via-ink/70 to-ink/10" />
      <div aria-hidden="true" className="absolute inset-x-0 top-0 h-44 bg-gradient-to-b from-ink/90 to-transparent" />
      <div className="absolute left-12 top-8 xl:left-16">
        <Logo height={28} />
      </div>
      <div className="relative flex max-w-lg flex-col gap-8">
        <ChargeBar level={0.78} cells={24} tone="onDark" className="h-3 opacity-90" />
        <h2 className="font-display text-4xl font-semibold leading-[1.1] text-white xl:text-5xl">{t.panel.title}</h2>
        <ul className="flex flex-col gap-5">
          {t.panel.items.map((item) => (
            <li key={item.title} className="flex flex-col gap-1 border-l-2 border-primary pl-4">
              <p className="text-sm font-semibold text-white">{item.title}</p>
              <p className="text-sm text-ink-text/75">{item.text}</p>
            </li>
          ))}
        </ul>
        <p className="flex items-center gap-2 text-xs text-ink-text/75">
          <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="size-4 shrink-0">
            <rect x="5" y="11" width="14" height="9" rx="2" />
            <path d="M8 11V8a4 4 0 0 1 8 0v3" strokeLinecap="round" />
          </svg>
          {t.panel.security}
        </p>
      </div>
    </aside>
  );
}
