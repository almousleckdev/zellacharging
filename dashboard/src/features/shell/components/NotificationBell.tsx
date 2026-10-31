"use client";

import Link from "next/link";
import { useIncidentSummary } from "@/features/incidents/api/useIncidentSummary";
import { useSession } from "@/features/session/SessionProvider";
import { usePendingWithdrawals } from "@/features/withdrawals/api/usePendingWithdrawals";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "@/shared/ui/Icon";

export function NotificationBell() {
  const { t } = useI18n();
  const { country, profile, scopedHref } = useSession();
  const canReview = profile.permissions.includes("WITHDRAWALS_REVIEW");
  const withdrawals = usePendingWithdrawals(country, canReview);
  const incidents = useIncidentSummary(country);
  const pending = (withdrawals.data?.total ?? 0) + (incidents.data?.openHigh ?? 0);

  return (
    <Link
      href={scopedHref(canReview && (withdrawals.data?.total ?? 0) > 0 ? "/withdrawals" : "/incidents")}
      aria-label={t.shell.notifications}
      className="relative inline-flex size-10 items-center justify-center rounded-control border border-border bg-background text-muted hover:text-foreground"
    >
      <Icon name="bell" className="size-[18px]" />
      {pending > 0 && <span aria-hidden="true" className="absolute right-2.5 top-2.5 size-2 rounded-full bg-primary ring-2 ring-background" />}
    </Link>
  );
}
