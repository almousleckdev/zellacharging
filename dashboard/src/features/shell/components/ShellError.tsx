"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { Logo } from "@/shared/ui/Logo";

export function ShellError({ onRetry }: { onRetry: () => void }) {
  const { t } = useI18n();

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 bg-surface px-6">
      <Logo height={26} />
      <div className="flex w-full max-w-sm flex-col gap-4">
        <Alert>{t.shell.loadError}</Alert>
        <Button onClick={onRetry} fullWidth>
          {t.common.retry}
        </Button>
      </div>
    </div>
  );
}
