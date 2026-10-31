import type { ReactNode } from "react";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";
import { Logo } from "@/shared/ui/Logo";

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-canvas px-4 py-6 sm:px-10">
      <header className="mx-auto flex w-full max-w-6xl items-center justify-between">
        <Logo height={26} />
        <LanguageSwitch />
      </header>
      <main className="flex flex-1 items-center justify-center py-10">
        <div className="w-full max-w-md rounded-card border border-border bg-background p-8 shadow-card">{children}</div>
      </main>
    </div>
  );
}
