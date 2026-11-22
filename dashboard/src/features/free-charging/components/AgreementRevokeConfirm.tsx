"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { useToast } from "@/shared/ui/ToastProvider";
import { useAgreementMutations } from "../api/useAgreementMutations";
import type { Agreement } from "../schemas";

type AgreementRevokeConfirmProps = {
  agreement: Agreement;
  onClose: () => void;
};

export function AgreementRevokeConfirm({ agreement, onClose }: AgreementRevokeConfirmProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { revoke } = useAgreementMutations();
  const [serverError, setServerError] = useState<string | null>(null);
  const labels = t.freeCharging.revoke;

  function confirm() {
    setServerError(null);
    revoke.mutate(agreement.id, {
      onSuccess: () => {
        notify("success", t.freeCharging.toasts.revoked);
        onClose();
      },
      onError: (error) => setServerError(errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
    });
  }

  return (
    <Modal
      open
      busy={revoke.isPending}
      title={labels.title}
      description={interpolate(labels.message, { name: agreement.driverName })}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={revoke.isPending} autoFocus>
            {t.dialogs.cancel}
          </Button>
          <Button variant="danger" onClick={confirm} loading={revoke.isPending}>
            {labels.confirm}
          </Button>
        </>
      }
    >
      {serverError && <Alert>{serverError}</Alert>}
    </Modal>
  );
}
