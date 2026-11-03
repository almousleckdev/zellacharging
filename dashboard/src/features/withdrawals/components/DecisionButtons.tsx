"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import type { WithdrawalItem } from "../schemas";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import type { WithdrawalAction } from "../api/useReviewWithdrawal";
import { DecisionDialog } from "./DecisionDialog";

const REFUNDABLE_STATES = ["REQUESTED", "PROCESSING", "REVIEW"];
const COMPACT = "h-8 px-3";

export function DecisionButtons({ withdrawal, compact = false }: { withdrawal: WithdrawalItem; compact?: boolean }) {
  const { t } = useI18n();
  const { profile } = useSession();
  const [action, setAction] = useState<WithdrawalAction | null>(null);
  const awaitingApproval = withdrawal.state === "PENDING_APPROVAL";
  const refundable = REFUNDABLE_STATES.includes(withdrawal.state);

  if (!profile.permissions.includes("WITHDRAWALS_REVIEW") || !(awaitingApproval || refundable)) {
    return null;
  }

  return (
    <div className="flex justify-end gap-2" onClick={(event) => event.stopPropagation()}>
      {awaitingApproval ? (
        <>
          <Button variant="secondary" className={compact ? COMPACT : undefined} onClick={() => setAction("reject")}>
            {t.withdrawals.actions.reject}
          </Button>
          <Button className={compact ? COMPACT : undefined} onClick={() => setAction("approve")}>{t.withdrawals.actions.approve}</Button>
        </>
      ) : (
        <Button variant="secondary" className={compact ? COMPACT : undefined} onClick={() => setAction("refund")}>
          {t.withdrawals.actions.refund}
        </Button>
      )}
      {action && <DecisionDialog withdrawal={withdrawal} action={action} onClose={() => setAction(null)} />}
    </div>
  );
}
