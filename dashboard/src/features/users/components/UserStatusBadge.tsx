"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";

const TONES = { ACTIVE: "success", INVITED: "warning" } as const;

export function UserStatusBadge({ status }: { status: string }) {
  const { t } = useI18n();
  const labels: Record<string, string> = t.users.statuses;
  const tone = status in TONES ? TONES[status as keyof typeof TONES] : "neutral";

  return <Badge tone={tone}>{labels[status] ?? status}</Badge>;
}
