"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Select } from "@/shared/ui/Select";

type PaymentsToolbarProps = {
  text: string;
  kind: string;
  state: string;
  onText: (value: string) => void;
  onKind: (value: string) => void;
  onState: (value: string) => void;
};

const KINDS = ["TOPUP", "WITHDRAWAL"] as const;
const STATES = ["SUCCEEDED", "PAID", "PENDING", "PROCESSING", "REQUESTED", "REVIEW", "FAILED"] as const;

export function PaymentsToolbar({ text, kind, state, onText, onKind, onState }: PaymentsToolbarProps) {
  const { t } = useI18n();
  const kinds: Record<string, string> = t.payments.kinds;
  const states: Record<string, string> = t.payments.states;

  return (
    <div className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
      <SearchInput label={t.payments.searchLabel} placeholder={t.payments.searchPlaceholder} value={text} onSearch={onText} />
      <div className="flex w-full gap-3 sm:w-auto">
        <div className="w-full sm:w-44">
          <Select
            label={t.payments.kindLabel}
            hideLabel
            options={[{ value: "", label: t.payments.allKinds }, ...KINDS.map((value) => ({ value, label: kinds[value] }))]}
            value={kind}
            onChange={(event) => onKind(event.target.value)}
          />
        </div>
        <div className="w-full sm:w-44">
          <Select
            label={t.payments.stateLabel}
            hideLabel
            options={[{ value: "", label: t.payments.allStates }, ...STATES.map((value) => ({ value, label: states[value] }))]}
            value={state}
            onChange={(event) => onState(event.target.value)}
          />
        </div>
      </div>
    </div>
  );
}
