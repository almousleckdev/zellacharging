"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Select } from "@/shared/ui/Select";

type SessionsToolbarProps = {
  text: string;
  state: string;
  onText: (value: string) => void;
  onState: (value: string) => void;
};

const STATES = ["ACTIVE", "STOPPING", "COMPLETED", "FAILED", "ABANDONED"] as const;

export function SessionsToolbar({ text, state, onText, onState }: SessionsToolbarProps) {
  const { t } = useI18n();
  const states: Record<string, string> = t.sessions.states;

  return (
    <div className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
      <SearchInput label={t.sessions.searchLabel} placeholder={t.sessions.searchPlaceholder} value={text} onSearch={onText} />
      <div className="w-full sm:w-56">
        <Select
          label={t.sessions.stateLabel}
          hideLabel
          options={[{ value: "", label: t.sessions.allStates }, ...STATES.map((value) => ({ value, label: states[value] }))]}
          value={state}
          onChange={(event) => onState(event.target.value)}
        />
      </div>
    </div>
  );
}
