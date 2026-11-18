"use client";

import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { Select } from "@/shared/ui/Select";
import { TextField } from "@/shared/ui/TextField";
import type { FreeAccessCategory, FreeAccessRequest } from "../api/useFreeAccess";

type GiveFreeAccessDialogProps = {
  open: boolean;
  loading: boolean;
  onSubmit: (request: FreeAccessRequest) => void;
  onClose: () => void;
};

export function GiveFreeAccessDialog({ open, loading, onSubmit, onClose }: GiveFreeAccessDialogProps) {
  const { t } = useI18n();
  const labels = t.customers.freeAccess;
  const [reason, setReason] = useState("");
  const [until, setUntil] = useState("");
  const [category, setCategory] = useState<FreeAccessCategory>("NDZELA_STAFF");
  const categories: Record<string, string> = labels.categories;

  const submit = () => onSubmit({ reason: reason.trim(), category, expiresAt: until ? `${until}T23:59:59Z` : null });

  return (
    <Modal
      open={open}
      title={labels.give}
      busy={loading}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={loading}>
            {t.dialogs.cancel}
          </Button>
          <Button onClick={submit} loading={loading} disabled={reason.trim() === ""}>
            {labels.confirmGive}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        <Select
          label={labels.categoryLabel}
          value={category}
          onChange={(event) => setCategory(event.target.value as FreeAccessCategory)}
          options={(["NDZELA_STAFF", "OTHER"] as const).map((value) => ({ value, label: categories[value] }))}
        />
        <TextField label={labels.reasonLabel} value={reason} maxLength={200} onChange={(event) => setReason(event.target.value)} />
        <TextField label={labels.untilLabel} type="date" value={until} onChange={(event) => setUntil(event.target.value)} />
      </div>
    </Modal>
  );
}
