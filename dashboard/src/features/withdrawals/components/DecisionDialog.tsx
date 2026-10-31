"use client";

import { useState } from "react";
import type { PaymentItem } from "@/features/payments/schemas";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { useToast } from "@/shared/ui/ToastProvider";
import { useReviewWithdrawal, type WithdrawalAction } from "../api/useReviewWithdrawal";

type DecisionDialogProps = {
  withdrawal: PaymentItem;
  action: WithdrawalAction;
  onClose: () => void;
};

export function DecisionDialog({ withdrawal, action, onClose }: DecisionDialogProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const { notify } = useToast();
  const review = useReviewWithdrawal();
  const [note, setNote] = useState("");
  const [missing, setMissing] = useState(false);
  const labels = t.withdrawals[action];
  const reasonRequired = action !== "approve";

  const amount = format.money([{ currency: withdrawal.currency, decimals: withdrawal.decimals, minor: withdrawal.amountMinor }])[0];
  const description = interpolate(labels.description, {
    amount,
    customer: withdrawal.customerName,
    operator: withdrawal.operator,
    number: withdrawal.msisdn,
  });

  const submit = () => {
    if (reasonRequired && note.trim() === "") {
      setMissing(true);
      return;
    }
    review.mutate(
      { country: withdrawal.country, id: withdrawal.id, action, note: note.trim() },
      {
        onSuccess: () => {
          notify("success", labels.saved);
          onClose();
        },
        onError: (error) => {
          notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN"));
          onClose();
        },
      },
    );
  };

  return (
    <Modal
      open
      title={labels.title}
      description={description}
      busy={review.isPending}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={review.isPending}>
            {t.dialogs.cancel}
          </Button>
          <Button onClick={submit} loading={review.isPending}>
            {labels.confirm}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-1.5">
        <label className="flex flex-col gap-1.5 text-sm font-medium text-foreground">
          {labels.noteLabel}
          <textarea
            value={note}
            maxLength={300}
            rows={3}
            onChange={(event) => {
              setNote(event.target.value);
              setMissing(false);
            }}
            placeholder={labels.notePlaceholder}
            aria-invalid={missing}
            className="rounded-control border border-border-strong bg-background p-3 text-sm font-normal"
          />
        </label>
        {missing && reasonRequired && (
          <span role="alert" className="text-xs text-danger">
            {action === "refund" ? t.withdrawals.refund.noteRequired : t.withdrawals.reject.noteRequired}
          </span>
        )}
      </div>
    </Modal>
  );
}
