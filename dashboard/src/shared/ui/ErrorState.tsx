"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "./Button";
import { Icon } from "./Icon";

export function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  const { t } = useI18n();

  return (
    <div role="alert" className="flex flex-col items-center gap-4 rounded-card border border-danger-border bg-danger-soft px-6 py-12 text-center">
      <Icon name="alert" className="size-6 text-danger" />
      <p className="text-sm font-medium text-danger">{message}</p>
      <Button variant="secondary" onClick={onRetry}>
        {t.common.retry}
      </Button>
    </div>
  );
}
