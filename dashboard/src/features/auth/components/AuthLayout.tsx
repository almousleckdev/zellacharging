import type { ReactNode } from "react";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";
import { Logo } from "@/shared/ui/Logo";
import { BrandPanel } from "./BrandPanel";

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="grid min-h-screen bg-background lg:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)]">
      <main className="flex flex-col px-6 py-6 sm:px-12">
        <header className="flex items-center justify-between">
          <Logo height={26} />
          <LanguageSwitch />
        </header>
        <div className="flex flex-1 items-center justify-center py-12">
          <div className="w-full max-w-sm">{children}</div>
        </div>
      </main>
      <BrandPanel />
    </div>
  );
}
