"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import type { PaymentItem } from "@/features/payments/schemas";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import type { WithdrawalAction } from "../api/useReviewWithdrawal";
import { DecisionDialog } from "./DecisionDialog";

const REFUNDABLE_STATES = ["REQUESTED", "PROCESSING", "REVIEW"];

export function DecisionButtons({ withdrawal }: { withdrawal: PaymentItem }) {
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
          <Button variant="secondary" onClick={() => setAction("reject")}>
            {t.withdrawals.actions.reject}
          </Button>
          <Button onClick={() => setAction("approve")}>{t.withdrawals.actions.approve}</Button>
        </>
      ) : (
        <Button variant="secondary" onClick={() => setAction("refund")}>
          {t.withdrawals.actions.refund}
        </Button>
      )}
      {action && <DecisionDialog withdrawal={withdrawal} action={action} onClose={() => setAction(null)} />}
    </div>
  );
}
