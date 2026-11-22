"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";

const TONES = { ACTIVE: "success", SUSPENDED: "warning", REVOKED: "danger" } as const;

export function AgreementStatusBadge({ status }: { status: string }) {
  const { t } = useI18n();
  const labels: Record<string, string> = t.freeCharging.statuses;
  const tone = status in TONES ? TONES[status as keyof typeof TONES] : "neutral";

  return <Badge tone={tone}>{labels[status] ?? status}</Badge>;
}
