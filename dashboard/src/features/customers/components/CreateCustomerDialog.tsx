"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { Select } from "@/shared/ui/Select";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCreateCustomer } from "../api/useCreateCustomer";

const EMPTY = { firstName: "", lastName: "", email: "", phone: "" };

export function CreateCustomerDialog({ onClose }: { onClose: () => void }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { profile, country: current } = useSession();
  const create = useCreateCustomer();
  const labels = t.customers.create;
  const [country, setCountry] = useState(current);
  const [form, setForm] = useState(EMPTY);
  const set = (key: keyof typeof EMPTY) => (event: { target: { value: string } }) => setForm({ ...form, [key]: event.target.value });
  const valid = form.firstName.trim() !== "" && form.lastName.trim() !== "" && (form.email.trim() !== "" || form.phone.trim() !== "");

  const submit = () =>
    create.mutate(
      { country, firstName: form.firstName.trim(), lastName: form.lastName.trim(), email: form.email.trim(), phone: form.phone.trim() },
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
      description={labels.hint}
      busy={create.isPending}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={create.isPending}>
            {t.dialogs.cancel}
          </Button>
          <Button onClick={submit} loading={create.isPending} disabled={!valid}>
            {labels.submit}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        {profile.countries.length > 1 && (
          <Select label={t.customers.detail.country} value={country} onChange={(event) => setCountry(event.target.value)} options={profile.countries.map((entry) => ({ value: entry.code, label: entry.name }))} />
        )}
        <TextField label={t.customers.columns.firstName} value={form.firstName} maxLength={80} onChange={set("firstName")} />
        <TextField label={t.customers.columns.lastName} value={form.lastName} maxLength={80} onChange={set("lastName")} />
        <TextField label={t.customers.detail.email} type="email" value={form.email} onChange={set("email")} />
        <TextField label={t.customers.detail.phone} type="tel" value={form.phone} hint={labels.phoneHint} onChange={set("phone")} />
      </div>
    </Modal>
  );
}
