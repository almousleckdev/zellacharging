"use client";

import Link from "next/link";
import { paymentListSchema } from "@/features/payments/schemas";
import { signedAmount } from "@/features/payments/format";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useLogList } from "@/shared/lists/useLogList";
import { cn } from "@/shared/lib/cn";
import { timeAgo } from "@/shared/lib/timeAgo";
import { Card } from "@/shared/ui/Card";
import { Icon } from "@/shared/ui/Icon";

const RECENT = 5;

function ActivityCard() {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const { country, scopedHref } = useSession();
  const query = useLogList("payments", { country, text: "", from: "", to: "", page: 0, size: RECENT, sort: "", extra: {} }, paymentListSchema);
  const labels = t.overview.activity;
  const kinds: Record<string, string> = t.payments.kinds;
  const items = query.data?.items ?? [];

  return (
    <Card
      title={labels.title}
      action={
        <Link href={scopedHref("/payments")} className="text-sm font-medium text-primary-text hover:underline">
          {labels.all}
        </Link>
      }
    >
      {items.length === 0 && query.data && <p className="text-sm text-subtle">{labels.empty}</p>}
      <ul className="flex flex-col gap-4">
        {items.map((item) => {
          const topup = item.kind === "TOPUP";
          return (
            <li key={`${item.kind}-${item.id}`} className="flex items-center gap-3">
              <span className={cn("flex size-9 shrink-0 items-center justify-center rounded-control", topup ? "bg-success-soft text-success" : "bg-primary-soft text-primary-text")}>
                <Icon name={topup ? "arrowDown" : "arrowUp"} className="size-4" />
              </span>
              <span className="flex min-w-0 flex-1 flex-col">
                <span className="truncate text-sm font-medium text-foreground">{kinds[item.kind] ?? item.kind} · {item.customerName}</span>
                <span className="text-xs text-subtle">{timeAgo(item.createdAt, locale)}</span>
              </span>
              <span className="figure shrink-0 text-sm font-semibold text-foreground">{signedAmount(item, format)}</span>
            </li>
          );
        })}
      </ul>
    </Card>
  );
}

export function RecentActivity() {
  const { profile } = useSession();

  return profile.permissions.includes("PAYMENTS_READ") ? <ActivityCard /> : null;
}
