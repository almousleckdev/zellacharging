"use client";

import { useState } from "react";
import type { ReactNode } from "react";
import { ProfileCompletionScreen } from "@/features/onboarding/components/ProfileCompletionScreen";
import { SessionProvider } from "@/features/session/SessionProvider";
import { useAdminProfile } from "@/features/session/useAdminProfile";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ShellError } from "./ShellError";
import { ShellSkeleton } from "./ShellSkeleton";
import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";

function ConsoleFrame({ children }: { children: ReactNode }) {
  const { t } = useI18n();
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="flex min-h-screen bg-canvas">
      <a
        href="#content"
        className="sr-only z-50 rounded-control bg-background px-3 py-2 text-sm font-medium focus:not-sr-only focus:fixed focus:left-3 focus:top-3"
      >
        {t.common.skipToContent}
      </a>
      <Sidebar open={menuOpen} onClose={() => setMenuOpen(false)} />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar onOpenMenu={() => setMenuOpen(true)} />
        <main id="content" className="mx-auto flex w-full max-w-[1360px] flex-1 flex-col gap-6 p-4 sm:p-6 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  const profile = useAdminProfile();

  if (profile.isPending) {
    return <ShellSkeleton />;
  }
  if (profile.isError) {
    return <ShellError onRetry={() => void profile.refetch()} />;
  }
  if (!profile.data.profileComplete) {
    return <ProfileCompletionScreen profile={profile.data} />;
  }
  return (
    <SessionProvider profile={profile.data}>
      <ConsoleFrame>{children}</ConsoleFrame>
    </SessionProvider>
  );
}
