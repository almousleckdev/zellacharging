"use client";

import { useState } from "react";
import type { CustomerItem } from "@/features/customers/schemas";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { TextArea } from "@/shared/ui/TextArea";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useAgreementMutations } from "../api/useAgreementMutations";
import type { Agreement } from "../schemas";
import { CONDITIONS_MAX, createFormSchema, termsErrorsOf, termsFormSchema } from "../termsForm";
import type { TermsErrors, TermsFormValues } from "../termsForm";
import { LinkedCustomerPicker } from "./LinkedCustomerPicker";

type AgreementFormDialogProps = {
  agreement: Agreement | null;
  onClose: () => void;
};

function initialValues(agreement: Agreement | null): TermsFormValues {
  return {
    customerId: "",
    startsOn: agreement?.startsOn ?? "",
    endsOn: agreement?.endsOn ?? "",
    conditions: agreement?.conditions ?? "",
  };
}

export function AgreementFormDialog({ agreement, onClose }: AgreementFormDialogProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { create, update } = useAgreementMutations();
  const [values, setValues] = useState(initialValues(agreement));
  const [picked, setPicked] = useState<CustomerItem | null>(null);
  const [errors, setErrors] = useState<TermsErrors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const labels = t.freeCharging.form;
  const validation: Record<string, string> = t.freeCharging.validation;
  const pending = create.isPending || update.isPending;
  const message = (field: keyof TermsFormValues) => (errors[field] ? interpolate(validation[errors[field]], { max: CONDITIONS_MAX }) : undefined);
  const change = (field: keyof TermsFormValues) => (event: { target: { value: string } }) => setValues({ ...values, [field]: event.target.value });

  function pick(customer: CustomerItem) {
    setPicked(customer);
    setValues({ ...values, customerId: customer.id });
  }

  function submit() {
    const parsed = (agreement ? termsFormSchema : createFormSchema).safeParse(values);
    if (!parsed.success) {
      setErrors(termsErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    setServerError(null);
    const { customerId, startsOn, endsOn, conditions } = parsed.data;
    const terms = { startsOn, ...(endsOn && { endsOn }), ...(conditions && { conditions }) };
    const callbacks = {
      onSuccess: () => {
        notify("success", agreement ? t.freeCharging.toasts.updated : t.freeCharging.toasts.created);
        onClose();
      },
      onError: (error: unknown) => setServerError(errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
    };
    if (agreement) {
      update.mutate({ id: agreement.id, ...terms }, callbacks);
    } else {
      create.mutate({ customerId, ...terms }, callbacks);
    }
  }

  return (
    <Modal
      open
      size="lg"
      busy={pending}
      title={agreement ? labels.editTitle : labels.createTitle}
      description={agreement?.driverName}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={pending}>
            {labels.cancel}
          </Button>
          <Button onClick={submit} loading={pending}>
            {pending ? labels.submitting : agreement ? labels.submitEdit : labels.submitCreate}
          </Button>
        </>
      }
    >
      <form onSubmit={(event) => { event.preventDefault(); submit(); }} noValidate className="flex flex-col gap-5">
        {serverError && <Alert>{serverError}</Alert>}
        {!agreement && <LinkedCustomerPicker selected={picked} onSelect={pick} error={message("customerId")} disabled={pending} />}
        <div className="grid gap-5 sm:grid-cols-2">
          <TextField label={labels.startsOn} type="date" value={values.startsOn} onChange={change("startsOn")} error={message("startsOn")} disabled={pending} />
          <TextField label={labels.endsOn} type="date" value={values.endsOn} onChange={change("endsOn")} error={message("endsOn")} disabled={pending} />
        </div>
        <TextArea
          label={labels.conditions}
          value={values.conditions}
          onChange={change("conditions")}
          hint={interpolate(labels.conditionsHint, { count: values.conditions.length, max: CONDITIONS_MAX })}
          error={message("conditions")}
          disabled={pending}
        />
      </form>
    </Modal>
  );
}
