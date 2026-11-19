"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "./Badge";

export function NdzelaDriverBadge() {
  const { t } = useI18n();

  return <Badge tone="warning">{t.common.ndzelaDriver}</Badge>;
}
