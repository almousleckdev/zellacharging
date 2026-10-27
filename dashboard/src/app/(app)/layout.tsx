import { Suspense } from "react";
import type { ReactNode } from "react";
import { AppShell } from "@/features/shell/components/AppShell";
import { ShellSkeleton } from "@/features/shell/components/ShellSkeleton";
import { QueryProvider } from "@/shared/api/QueryProvider";
import { ToastProvider } from "@/shared/ui/ToastProvider";

export default function AppLayout({ children }: { children: ReactNode }) {
  return (
    <QueryProvider>
      <ToastProvider>
        <Suspense fallback={<ShellSkeleton />}>
          <AppShell>{children}</AppShell>
        </Suspense>
      </ToastProvider>
    </QueryProvider>
  );
}
