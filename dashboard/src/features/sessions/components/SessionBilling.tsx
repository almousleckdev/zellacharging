"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";
import type { SessionDetail } from "../schemas";

export function SessionBilling({ detail }: { detail: SessionDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.sessions.detail;
  const { session } = detail;
  const currency = session.currency ?? "XAF";
  const decimals = session.decimals ?? 0;
  const money = (minor: number | null) => (minor === null ? t.common.none : format.money([{ currency, decimals, minor }])[0]);
  const total = session.costMinor === null ? null : session.costMinor + (detail.serviceFeeMinor ?? 0) + (detail.vatMinor ?? 0);
  const holds: Record<string, string> = labels.holdStates;

  return (
    <Card title={labels.billing}>
      <FactList
        facts={[
          { label: labels.price, value: detail.pricePerKwhMinor === null ? t.common.none : `${money(detail.pricePerKwhMinor)} / ${t.sessions.unitKwh}` },
          { label: labels.electricity, value: money(session.costMinor) },
          { label: labels.commission, value: money(detail.serviceFeeMinor) },
          { label: labels.vat, value: money(detail.vatMinor) },
          { label: labels.total, value: <span className="figure font-semibold">{money(total)}</span> },
          { label: labels.reserved, value: money(detail.reservedMinor) },
          { label: labels.captured, value: money(detail.capturedMinor) },
          { label: labels.hold, value: detail.holdState ? (holds[detail.holdState] ?? detail.holdState) : t.common.none },
        ]}
      />
    </Card>
  );
}
