"use client";

import Image from "next/image";
import type { ReactNode } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";
import { Logo } from "@/shared/ui/Logo";

export function AuthLayout({ children }: { children: ReactNode }) {
  const { t } = useI18n();

  return (
    <div className="grid min-h-screen w-full bg-background lg:grid-cols-2">
      <aside className="relative hidden overflow-hidden bg-sidebar lg:block">
        <Image src="/logo/image.png" alt="" fill priority sizes="50vw" className="object-cover object-center" />
        <div aria-hidden="true" className="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-slate-900/10 to-transparent" />
        <div aria-hidden="true" className="absolute inset-x-0 top-0 h-40 bg-gradient-to-b from-slate-900/60 to-transparent" />
        <div className="absolute left-10 top-10">
          <Logo height={36} />
        </div>
        <div className="absolute inset-x-0 bottom-0 px-10 pb-10 text-white">
          <p className="max-w-md font-display text-3xl font-semibold leading-tight">{t.login.heroTitle}</p>
          <p className="mt-3 max-w-md text-sm text-white/80">{t.login.heroText}</p>
        </div>
      </aside>
      <main className="relative flex items-center justify-center px-6 py-12 sm:px-12">
        <div className="absolute right-6 top-6">
          <LanguageSwitch />
        </div>
        <div className="w-full max-w-sm [&_input]:h-11">
          <div className="mb-10 rounded-control bg-sidebar px-4 py-3 lg:hidden">
            <Logo height={26} />
          </div>
          {children}
        </div>
      </main>
    </div>
  );
}
