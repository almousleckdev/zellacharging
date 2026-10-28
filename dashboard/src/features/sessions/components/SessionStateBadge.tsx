"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";

const TONES: Record<string, "success" | "warning" | "danger"> = {
  COMPLETED: "success",
  ACTIVE: "warning",
  STOPPING: "warning",
  START_ACCEPTED: "warning",
  REQUESTED: "warning",
  FAILED: "danger",
  ABANDONED: "danger",
};

export function SessionStateBadge({ state }: { state: string }) {
  const { t } = useI18n();
  const labels: Record<string, string> = t.sessions.states;

  return <Badge tone={TONES[state] ?? "neutral"}>{labels[state] ?? state}</Badge>;
}
