"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { FIELD } from "@/shared/ui/fieldStyles";
import { Modal } from "@/shared/ui/Modal";
import { useToast } from "@/shared/ui/ToastProvider";
import { useReviewIncident } from "../api/useReviewIncident";
import type { IncidentItem } from "../schemas";

type ReviewDialogProps = {
  incident: IncidentItem;
  status: "ACKNOWLEDGED" | "RESOLVED";
  onClose: () => void;
};

export function ReviewDialog({ incident, status, onClose }: ReviewDialogProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const review = useReviewIncident();
  const [note, setNote] = useState(incident.note ?? "");
  const labels = t.incidents.review;

  const submit = () =>
    review.mutate(
      { country: incident.country, key: incident.key, status, note },
      {
        onSuccess: () => {
          notify("success", labels.saved);
          onClose();
        },
        onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
      },
    );

  return (
    <Modal
      open
      title={status === "RESOLVED" ? labels.resolveTitle : labels.acknowledgeTitle}
      busy={review.isPending}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={review.isPending}>
            {t.dialogs.cancel}
          </Button>
          <Button onClick={submit} loading={review.isPending}>
            {status === "RESOLVED" ? t.incidents.actions.resolve : t.incidents.actions.acknowledge}
          </Button>
        </>
      }
    >
      <label className="flex flex-col gap-1.5 text-sm font-medium text-foreground">
        {labels.noteLabel}
        <textarea
          value={note}
          maxLength={500}
          rows={4}
          onChange={(event) => setNote(event.target.value)}
          placeholder={labels.notePlaceholder}
          className={`p-3 font-normal ${FIELD}`}
        />
      </label>
    </Modal>
  );
}
