"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { SegmentBar } from "@/shared/ui/SegmentBar";
import type { MoneySplit } from "../moneySplit";

function Figure({ label, hint, value, tone }: { label: string; hint: string; value: string; tone: "owed" | "earned" | "flight" }) {
  const dot = { owed: "bg-steel", earned: "bg-primary", flight: "bg-ink-text/30" }[tone];
  return (
    <div className="flex flex-col gap-1">
      <p className="flex items-center gap-2 text-sm text-ink-muted">
        <span aria-hidden="true" className={`size-2.5 rounded-full ${dot}`} />
        {label}
      </p>
      <p className="figure text-2xl font-semibold text-white">{value}</p>
      <p className="text-xs text-ink-muted">{hint}</p>
    </div>
  );
}

export function MoneyPanel({ split }: { split: MoneySplit }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.money;
  const amount = (minor: number) => format.money([{ currency: split.currency, decimals: split.decimals, minor }])[0];

  return (
    <section className="flex flex-col gap-8 rounded-panel bg-ink p-6 text-ink-text sm:p-8">
      <header className="flex flex-col gap-1">
        <h2 className="font-display text-lg font-semibold text-white">{labels.title}</h2>
        <p className="text-sm text-ink-muted">{labels.subtitle}</p>
      </header>
      <div className="flex flex-col gap-3">
        <p className="text-sm text-ink-muted">{labels.held}</p>
        <p className="figure text-5xl font-semibold text-white sm:text-6xl">{amount(split.heldMinor)}</p>
        <SegmentBar
          label={labels.barLabel}
          className="mt-3"
          segments={[
            { amount: split.earnedMinor, className: "bg-primary" },
            { amount: split.owedMinor, className: "bg-steel" },
            { amount: split.inFlightMinor, className: "bg-ink-text/30" },
          ]}
        />
      </div>
      <div className="grid gap-6 sm:grid-cols-3">
        <Figure tone="owed" label={labels.owed} hint={labels.owedHint} value={amount(split.owedMinor)} />
        <Figure tone="earned" label={labels.earned} hint={labels.earnedHint} value={amount(split.earnedMinor)} />
        <Figure tone="flight" label={labels.inFlight} hint={labels.inFlightHint} value={amount(split.inFlightMinor)} />
      </div>
      <footer className="flex flex-wrap items-baseline gap-x-3 gap-y-1 border-t border-ink-line pt-5">
        <p className="text-sm text-ink-muted">{labels.safe}</p>
        <p className="figure text-xl font-semibold text-primary">{amount(split.earnedMinor)}</p>
        <p className="text-xs text-ink-muted">{labels.safeHint}</p>
      </footer>
    </section>
  );
}
