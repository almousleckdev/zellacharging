"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import type { SessionItem } from "../schemas";

export function FreeBadge({ category }: { category: NonNullable<SessionItem["freeCategory"]> }) {
  const { t } = useI18n();

  return <Badge tone="info">{t.sessions.free[category]}</Badge>;
}
