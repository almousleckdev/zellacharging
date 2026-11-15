"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCustomerSupport } from "../api/useCustomerSupport";
import type { CustomerDetail } from "../schemas";

export function EditNameDialog({ customer, onClose }: { customer: CustomerDetail; onClose: () => void }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const support = useCustomerSupport(customer.country, customer.id);
  const labels = t.customers.support.name;
  const [firstName, setFirstName] = useState(customer.firstName);
  const [lastName, setLastName] = useState(customer.lastName);
  const valid = firstName.trim() !== "" && lastName.trim() !== "";

  const submit = () =>
    support.mutate(
      { kind: "name", firstName: firstName.trim(), lastName: lastName.trim() },
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
        <TextField label={t.customers.columns.firstName} value={firstName} maxLength={100} onChange={(event) => setFirstName(event.target.value)} />
        <TextField label={t.customers.columns.lastName} value={lastName} maxLength={100} onChange={(event) => setLastName(event.target.value)} />
      </div>
    </Modal>
  );
}
