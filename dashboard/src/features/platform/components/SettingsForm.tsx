"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Checkbox } from "@/shared/ui/Checkbox";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useSaveSettings } from "../api/useSaveSettings";
import { EDITABLE_FIELDS, changesOf, draftOf } from "../editableSettings";
import type { Draft } from "../editableSettings";
import type { SettingsSection } from "../schemas";

type SettingsFormProps = { section: SettingsSection; decimals: number; onClose: () => void };

export function SettingsForm({ section, decimals, onClose }: SettingsFormProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const save = useSaveSettings();
  const fields = EDITABLE_FIELDS[section.section] ?? [];
  const labels: Record<string, string> = t.platform.settings;
  const [initial] = useState(() => draftOf(fields, section.values, decimals));
  const [draft, setDraft] = useState<Draft>(initial);
  const changes = changesOf(fields, initial, draft, decimals);
  const step = decimals > 0 ? 10 ** -decimals : 1;

  const submit = () => {
    save.mutate(
      { section: section.section, values: changes },
      {
        onSuccess: () => {
          notify("success", t.platform.saved);
          onClose();
        },
        onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
      },
    );
  };

  return (
    <form
      className="flex flex-col gap-4"
      onSubmit={(event) => {
        event.preventDefault();
        submit();
      }}
    >
      <div className="grid gap-4 sm:grid-cols-2">
        {fields.map(({ key, kind }) =>
          kind === "flag" ? (
            <div key={key} className="sm:col-span-2">
              <Checkbox label={labels[key] ?? key} checked={draft[key] === true} onChange={(event) => setDraft({ ...draft, [key]: event.target.checked })} />
            </div>
          ) : (
            <TextField
              key={key}
              label={labels[key] ?? key}
              type="number"
              inputMode="decimal"
              min={0}
              max={kind === "percent" ? 100 : undefined}
              step={kind === "percent" ? 0.1 : step}
              value={String(draft[key])}
              onChange={(event) => setDraft({ ...draft, [key]: event.target.value })}
            />
          ),
        )}
      </div>
      <div className="flex justify-end gap-2">
        <Button variant="secondary" onClick={onClose} disabled={save.isPending}>
          {t.dialogs.cancel}
        </Button>
        <Button type="submit" loading={save.isPending} disabled={Object.keys(changes).length === 0}>
          {t.platform.save}
        </Button>
      </div>
    </form>
  );
}
