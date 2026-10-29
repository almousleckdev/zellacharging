"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";

export function StationOnlineBadge({ online }: { online: boolean }) {
  const { t } = useI18n();

  return <Badge tone={online ? "success" : "danger"}>{online ? t.stations.online : t.stations.offline}</Badge>;
}
