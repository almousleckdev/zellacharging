"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { timeAgo } from "@/shared/lib/timeAgo";
import { Card } from "@/shared/ui/Card";
import { OperatorBadge } from "@/shared/ui/OperatorBadge";
import { usePendingWithdrawals } from "../api/usePendingWithdrawals";
import { DecisionButtons } from "./DecisionButtons";

const VISIBLE = 3;

export function PendingWithdrawalsPanel() {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const { country, profile, scopedHref } = useSession();
  const canReview = profile.permissions.includes("WITHDRAWALS_REVIEW");
  const pending = usePendingWithdrawals(country, canReview, VISIBLE);
  const labels = t.overview.pending;
  const items = pending.data?.items ?? [];

  if (!canReview) {
    return null;
  }

  return (
    <Card
      title={labels.title}
      action={
        <Link href={scopedHref("/withdrawals")} className="text-sm font-medium text-primary-text hover:underline">
          {labels.all}
        </Link>
      }
    >
      {items.length === 0 && pending.data && <p className="text-sm text-subtle">{labels.empty}</p>}
      <ul className="flex flex-col gap-4">
        {items.map((item) => (
          <li key={item.id} className="flex flex-col gap-3 rounded-control bg-surface p-3">
            <div className="flex items-center justify-between gap-3">
              <div className="flex min-w-0 flex-col">
                <span className="truncate text-sm font-semibold text-foreground">{item.customerName}</span>
                <span className="flex items-center gap-1.5 text-xs text-subtle">
                  <OperatorBadge operator={item.operator} />
                  {timeAgo(item.createdAt, locale)}
                </span>
              </div>
              <span className="figure shrink-0 text-sm font-semibold text-foreground">{format.money([{ currency: item.currency, decimals: item.decimals, minor: item.amountMinor }])[0]}</span>
            </div>
            <DecisionButtons withdrawal={item} compact />
          </li>
        ))}
      </ul>
    </Card>
  );
}
