"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { useToast } from "@/shared/ui/ToastProvider";
import { useAgreementMutations } from "../api/useAgreementMutations";
import type { Agreement } from "../schemas";
import { termsErrorsOf, termsFormSchema } from "../termsForm";
import type { TermsErrors, TermsFormValues } from "../termsForm";
import { AgreementTermsFields } from "./AgreementTermsFields";

type AgreementEditDialogProps = { agreement: Agreement; onClose: () => void };

export function AgreementEditDialog({ agreement, onClose }: AgreementEditDialogProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { update } = useAgreementMutations();
  const [values, setValues] = useState<TermsFormValues>({ customerId: "", startsOn: agreement.startsOn, endsOn: agreement.endsOn ?? "", conditions: agreement.conditions ?? "" });
  const [errors, setErrors] = useState<TermsErrors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const labels = t.freeCharging.form;

  function submit() {
    const parsed = termsFormSchema.safeParse(values);
    if (!parsed.success) {
      setErrors(termsErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    setServerError(null);
    const { startsOn, endsOn, conditions } = parsed.data;
    update.mutate(
      { id: agreement.id, startsOn, ...(endsOn && { endsOn }), ...(conditions && { conditions }) },
      {
        onSuccess: () => {
          notify("success", t.freeCharging.toasts.updated);
          onClose();
        },
        onError: (error) => setServerError(errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
      },
    );
  }

  return (
    <Modal
      open
      size="lg"
      busy={update.isPending}
      title={labels.editTitle}
      description={agreement.driverName}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={update.isPending}>
            {labels.cancel}
          </Button>
          <Button onClick={submit} loading={update.isPending}>
            {update.isPending ? labels.submitting : labels.submitEdit}
          </Button>
        </>
      }
    >
      <form onSubmit={(event) => { event.preventDefault(); submit(); }} noValidate className="flex flex-col gap-5">
        {serverError && <Alert>{serverError}</Alert>}
        <AgreementTermsFields values={values} errors={errors} onChange={setValues} disabled={update.isPending} />
      </form>
    </Modal>
  );
}
