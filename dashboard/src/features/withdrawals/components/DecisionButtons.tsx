"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import type { PaymentItem } from "@/features/payments/schemas";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { DecisionDialog } from "./DecisionDialog";

export function DecisionButtons({ withdrawal }: { withdrawal: PaymentItem }) {
  const { t } = useI18n();
  const { profile } = useSession();
  const [action, setAction] = useState<"approve" | "reject" | null>(null);

  if (!profile.permissions.includes("WITHDRAWALS_REVIEW")) {
    return null;
  }

  return (
    <div className="flex justify-end gap-2" onClick={(event) => event.stopPropagation()}>
      <Button variant="secondary" onClick={() => setAction("reject")}>
        {t.withdrawals.actions.reject}
      </Button>
      <Button onClick={() => setAction("approve")}>{t.withdrawals.actions.approve}</Button>
      {action && <DecisionDialog withdrawal={withdrawal} action={action} onClose={() => setAction(null)} />}
    </div>
  );
}
