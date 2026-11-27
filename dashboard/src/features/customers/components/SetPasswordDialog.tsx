"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { PasswordField } from "@/shared/ui/PasswordField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCustomerSupport } from "../api/useCustomerSupport";
import type { CustomerDetail } from "../schemas";

const MINIMUM = 8;
const MAXIMUM = 72;

export function SetPasswordDialog({ customer, onClose }: { customer: CustomerDetail; onClose: () => void }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const support = useCustomerSupport(customer.country, customer.id);
  const labels = t.customers.support.password;
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const mismatch = confirmation !== "" && confirmation !== password;
  const valid = password.length >= MINIMUM && password.trim() !== "" && confirmation === password;

  const submit = () =>
    support.mutate(
      { kind: "password", password },
      {
        onSuccess: () => {
          notify("success", labels.done);
          onClose();
        },
        onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
      },
    );

  return (
    <Modal
      open
      title={labels.title}
      description={labels.message}
      busy={support.isPending}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={support.isPending}>
            {t.dialogs.cancel}
          </Button>
          <Button onClick={submit} loading={support.isPending} disabled={!valid}>
            {labels.confirm}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        <PasswordField label={labels.field} value={password} maxLength={MAXIMUM} autoComplete="new-password" onChange={(event) => setPassword(event.target.value)} />
        <PasswordField label={labels.confirmField} value={confirmation} maxLength={MAXIMUM} autoComplete="new-password" onChange={(event) => setConfirmation(event.target.value)} />
        <p className="text-sm text-muted">{mismatch ? labels.mismatch : labels.rules}</p>
      </div>
    </Modal>
  );
}
