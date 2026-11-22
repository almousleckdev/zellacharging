"use client";

import { useState } from "react";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { useToast } from "@/shared/ui/ToastProvider";
import { useApplyChanges } from "../api/useApplyChanges";
import { NO_SELECTION, pendingCounts } from "../selection";
import type { Selection } from "../selection";
import { termsErrorsOf, termsFormSchema } from "../termsForm";
import type { TermsErrors, TermsFormValues } from "../termsForm";
import { AgreementTermsFields } from "./AgreementTermsFields";
import { BulkOutcome } from "./BulkOutcome";
import { DriverPicker } from "./DriverPicker";

const EMPTY: TermsFormValues = { customerId: "", startsOn: "", endsOn: "", conditions: "" };

export function AgreementCreateDialog({ onClose }: { onClose: () => void }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const apply = useApplyChanges();
  const [selection, setSelection] = useState<Selection>(NO_SELECTION);
  const [values, setValues] = useState<TermsFormValues>(EMPTY);
  const [errors, setErrors] = useState<TermsErrors>({});
  const [empty, setEmpty] = useState(false);
  const labels = t.freeCharging.form;
  const counts = pendingCounts(selection);
  const finished = !apply.running && apply.result !== null;

  async function submit() {
    const needsTerms = counts.add > 0;
    const parsed = termsFormSchema.safeParse(needsTerms ? values : { ...values, startsOn: values.startsOn || "-" });
    setEmpty(counts.add + counts.remove === 0);
    setErrors(parsed.success ? {} : termsErrorsOf(parsed.error));
    if (!parsed.success || counts.add + counts.remove === 0) {
      return;
    }
    const { startsOn, endsOn, conditions } = parsed.data;
    const failures = await apply.run(Object.values(selection.adds), Object.values(selection.removes), { startsOn, ...(endsOn && { endsOn }), ...(conditions && { conditions }) });
    if (failures.length === 0) {
      notify("success", interpolate(t.freeCharging.bulk.saved, { add: counts.add, remove: counts.remove }));
      onClose();
    } else {
      const left = new Set(failures.map((failure) => failure.driver.driverId));
      setSelection({ adds: Object.fromEntries(Object.entries(selection.adds).filter(([id]) => left.has(id))), removes: Object.fromEntries(Object.entries(selection.removes).filter(([id]) => left.has(id))) });
    }
  }

  return (
    <Modal
      open
      size="xl"
      busy={apply.running}
      title={labels.createTitle}
      description={labels.createDescription}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={apply.running}>
            {finished ? t.freeCharging.bulk.close : labels.cancel}
          </Button>
          <Button onClick={() => void submit()} loading={apply.running}>
            {apply.progress ? interpolate(t.freeCharging.bulk.progress, apply.progress) : finished ? t.freeCharging.bulk.retry : labels.save}
          </Button>
        </>
      }
    >
      <form onSubmit={(event) => { event.preventDefault(); void submit(); }} noValidate className="flex flex-col gap-5">
        {apply.result && finished && <BulkOutcome result={apply.result} />}
        <DriverPicker selection={selection} onChange={setSelection} onClear={() => setSelection(NO_SELECTION)} disabled={apply.running} />
        {empty && <p role="alert" className="text-sm text-danger">{t.freeCharging.validation.nothingToDo}</p>}
        {counts.add > 0 && <AgreementTermsFields values={values} errors={errors} onChange={setValues} disabled={apply.running} />}
      </form>
    </Modal>
  );
}
