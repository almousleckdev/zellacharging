"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";

const TONES: Record<string, "success" | "warning" | "danger" | "neutral"> = {
  Available: "success",
  Preparing: "warning",
  Charging: "warning",
  Finishing: "warning",
  Reserved: "warning",
  SuspendedEV: "warning",
  SuspendedEVSE: "warning",
  Unavailable: "neutral",
  Faulted: "danger",
};

export function ConnectorStatusBadge({ status }: { status: string | null }) {
  const { t } = useI18n();
  const labels: Record<string, string> = t.stations.connectorStatuses;

  if (!status) {
    return <Badge>{t.common.none}</Badge>;
  }
  return <Badge tone={TONES[status] ?? "neutral"}>{labels[status] ?? status}</Badge>;
}
