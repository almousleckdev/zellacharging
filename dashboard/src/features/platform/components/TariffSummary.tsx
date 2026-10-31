"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { SettingsSection } from "../schemas";

type TariffSummaryProps = { sections: SettingsSection[]; currency: string; decimals: number };

export function TariffSummary({ sections, currency, decimals }: TariffSummaryProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels: Record<string, string> = t.platform.settings;
  const charging = sections.find((section) => section.section === "charging")?.values ?? {};
  const wallet = sections.find((section) => section.section === "wallet")?.values ?? {};
  const percent = (value: unknown) => (typeof value === "number" ? `${format.number(value, value % 1 === 0 ? 0 : 1)} %` : t.common.none);
  const price = charging.pricePerKwhMinor;

  return (
    <div className="grid gap-4 sm:grid-cols-2 min-[1700px]:grid-cols-4">
      <MetricCard
        label={labels.pricePerKwhMinor}
        icon="bolt"
        value={typeof price === "number" ? format.money([{ currency, decimals, minor: price }])[0] : t.common.none}
      />
      <MetricCard label={labels.serviceFeePercent} icon="percent" value={percent(charging.serviceFeePercent)} />
      <MetricCard label={labels.vatPercent} icon="tag" value={percent(charging.vatPercent)} />
      <MetricCard label={labels.withdrawalsEnabled} icon="wallet" value={wallet.withdrawalsEnabled === true ? t.platform.on : t.platform.off} />
    </div>
  );
}
