"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";

const TONES: Record<string, "success" | "warning" | "danger"> = {
  SUCCEEDED: "success",
  PAID: "success",
  PENDING: "warning",
  REQUESTED: "warning",
  PROCESSING: "warning",
  REVIEW: "warning",
  FAILED: "danger",
};

export function PaymentStateBadge({ state }: { state: string }) {
  const { t } = useI18n();
  const labels: Record<string, string> = t.payments.states;

  return <Badge tone={TONES[state] ?? "neutral"}>{labels[state] ?? state}</Badge>;
}
