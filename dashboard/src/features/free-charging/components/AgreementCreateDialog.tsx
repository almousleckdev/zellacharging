"use client";

import { useState } from "react";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCreateMany } from "../api/useCreateMany";
import type { DriverOption } from "../schemas";
import { termsErrorsOf, termsFormSchema } from "../termsForm";
import type { TermsErrors, TermsFormValues } from "../termsForm";
import { AgreementTermsFields } from "./AgreementTermsFields";
import { BulkOutcome } from "./BulkOutcome";
import { DriverMultiPicker } from "./DriverMultiPicker";

const EMPTY: TermsFormValues = { customerId: "", startsOn: "", endsOn: "", conditions: "" };

export function AgreementCreateDialog({ onClose }: { onClose: () => void }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const many = useCreateMany();
  const [selected, setSelected] = useState<DriverOption[]>([]);
  const [values, setValues] = useState<TermsFormValues>(EMPTY);
  const [errors, setErrors] = useState<TermsErrors>({});
  const [missing, setMissing] = useState(false);
  const labels = t.freeCharging.form;
  const finished = !many.running && (many.added > 0 || many.failures.length > 0);
  const label = selected.length > 1 ? interpolate(labels.submitCreateMany, { count: selected.length }) : labels.submitCreate;

  async function submit() {
    const parsed = termsFormSchema.safeParse(values);
    setMissing(selected.length === 0);
    setErrors(parsed.success ? {} : termsErrorsOf(parsed.error));
    if (!parsed.success || selected.length === 0) {
      return;
    }
    const { startsOn, endsOn, conditions } = parsed.data;
    const failed = await many.run(selected, { startsOn, ...(endsOn && { endsOn }), ...(conditions && { conditions }) });
    if (failed.length === 0) {
      notify("success", selected.length === 1 ? t.freeCharging.toasts.created : interpolate(t.freeCharging.bulk.addedMany, { count: selected.length }));
      onClose();
    } else {
      setSelected(failed.map((failure) => failure.driver));
    }
  }

  return (
    <Modal
      open
      size="lg"
      busy={many.running}
      title={labels.createTitle}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={many.running}>
            {finished ? t.freeCharging.bulk.close : labels.cancel}
          </Button>
          <Button onClick={() => void submit()} loading={many.running}>
            {many.progress ? interpolate(t.freeCharging.bulk.progress, many.progress) : finished ? t.freeCharging.bulk.retry : label}
          </Button>
        </>
      }
    >
      <form onSubmit={(event) => { event.preventDefault(); void submit(); }} noValidate className="flex flex-col gap-5">
        {finished && <BulkOutcome added={many.added} failures={many.failures} />}
        <DriverMultiPicker selected={selected} onChange={setSelected} disabled={many.running} error={missing ? t.freeCharging.validation.customerRequired : undefined} />
        <AgreementTermsFields values={values} errors={errors} onChange={setValues} disabled={many.running} />
      </form>
    </Modal>
  );
}
