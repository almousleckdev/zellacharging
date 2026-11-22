"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { Checkbox } from "@/shared/ui/Checkbox";
import { Modal } from "@/shared/ui/Modal";
import { Select } from "@/shared/ui/Select";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useChangeWindow } from "../api/useFreeWindow";
import { WEEKDAYS } from "../schemas";
import type { FreeWindow } from "../schemas";
import { windowErrorsOf, windowFormSchema } from "../windowForm";
import type { WindowErrors, WindowFormValues } from "../windowForm";

type WindowDialogProps = { window: FreeWindow; onClose: () => void };

export function WindowDialog({ window, onClose }: WindowDialogProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const change = useChangeWindow();
  const labels = t.freeCharging.window;
  const validation: Record<string, string> = labels.validation;
  const dayLabels: Record<string, string> = labels.dayOptions;
  const [values, setValues] = useState<WindowFormValues>({ day: window.day, from: window.from, until: window.endOfDay ? "" : (window.until ?? ""), endOfDay: window.endOfDay });
  const [errors, setErrors] = useState<WindowErrors>({});
  const [serverError, setServerError] = useState<string | null>(null);

  function submit() {
    const parsed = windowFormSchema.safeParse(values);
    if (!parsed.success) {
      setErrors(windowErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    setServerError(null);
    const { day, from, until, endOfDay } = parsed.data;
    change.mutate(
      { day, from, until: endOfDay ? null : until, endOfDay },
      {
        onSuccess: () => {
          notify("success", labels.toast);
          onClose();
        },
        onError: (error) => setServerError(errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
      },
    );
  }

  return (
    <Modal
      open
      busy={change.isPending}
      title={labels.form.title}
      description={labels.form.description}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={change.isPending}>
            {labels.form.cancel}
          </Button>
          <Button onClick={submit} loading={change.isPending}>
            {change.isPending ? labels.form.submitting : labels.form.submit}
          </Button>
        </>
      }
    >
      <form onSubmit={(event) => { event.preventDefault(); submit(); }} noValidate className="flex flex-col gap-5">
        {serverError && <Alert>{serverError}</Alert>}
        <Select
          label={labels.form.day}
          value={values.day}
          options={WEEKDAYS.map((day) => ({ value: day, label: dayLabels[day] }))}
          onChange={(event) => setValues({ ...values, day: event.target.value as WindowFormValues["day"] })}
          disabled={change.isPending}
        />
        <div className="grid gap-5 sm:grid-cols-2">
          <TextField label={labels.form.from} type="time" value={values.from} onChange={(event) => setValues({ ...values, from: event.target.value })} error={errors.from ? validation[errors.from] : undefined} disabled={change.isPending} />
          <TextField label={labels.form.until} type="time" value={values.until} onChange={(event) => setValues({ ...values, until: event.target.value })} error={errors.until ? validation[errors.until] : undefined} disabled={change.isPending || values.endOfDay} />
        </div>
        <Checkbox label={labels.form.endOfDay} checked={values.endOfDay} onChange={(event) => setValues({ ...values, endOfDay: event.target.checked })} disabled={change.isPending} />
      </form>
    </Modal>
  );
}
