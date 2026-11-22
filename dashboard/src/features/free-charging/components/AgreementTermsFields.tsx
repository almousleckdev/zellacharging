"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { TextArea } from "@/shared/ui/TextArea";
import { TextField } from "@/shared/ui/TextField";
import { CONDITIONS_MAX } from "../termsForm";
import type { TermsErrors, TermsFormValues } from "../termsForm";

type AgreementTermsFieldsProps = {
  values: TermsFormValues;
  errors: TermsErrors;
  onChange: (values: TermsFormValues) => void;
  disabled?: boolean;
};

export function AgreementTermsFields({ values, errors, onChange, disabled }: AgreementTermsFieldsProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.form;
  const validation: Record<string, string> = t.freeCharging.validation;
  const message = (field: keyof TermsFormValues) => (errors[field] ? interpolate(validation[errors[field]], { max: CONDITIONS_MAX }) : undefined);
  const change = (field: keyof TermsFormValues) => (event: { target: { value: string } }) => onChange({ ...values, [field]: event.target.value });

  return (
    <>
      <div className="grid gap-5 sm:grid-cols-2">
        <TextField label={labels.startsOn} type="date" value={values.startsOn} onChange={change("startsOn")} error={message("startsOn")} disabled={disabled} />
        <TextField label={labels.endsOn} type="date" value={values.endsOn} onChange={change("endsOn")} hint={labels.endsOnHint} error={message("endsOn")} disabled={disabled} />
      </div>
      <TextArea
        label={labels.conditions}
        value={values.conditions}
        onChange={change("conditions")}
        hint={interpolate(labels.conditionsHint, { count: values.conditions.length, max: CONDITIONS_MAX })}
        error={message("conditions")}
        disabled={disabled}
      />
    </>
  );
}
