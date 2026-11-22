"use client";

import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Spinner } from "@/shared/ui/Spinner";
import { useDriverSearch } from "../api/useDriverSearch";
import type { DriverOption } from "../schemas";
import { DriverOptionRow } from "./DriverOptionRow";
import { SelectedDrivers } from "./SelectedDrivers";

type DriverMultiPickerProps = {
  selected: DriverOption[];
  onChange: (selected: DriverOption[]) => void;
  error?: string;
  disabled?: boolean;
};

const pickable = (option: DriverOption) => option.linked && !option.inProgram;

export function DriverMultiPicker({ selected, onChange, error, disabled }: DriverMultiPickerProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;
  const [text, setText] = useState("");
  const drivers = useDriverSearch(text);
  const items = drivers.data ?? [];
  const chosen = new Set(selected.map((driver) => driver.driverId));
  const open = items.filter((option) => pickable(option) && !chosen.has(option.driverId));

  const toggle = (option: DriverOption) =>
    onChange(chosen.has(option.driverId) ? selected.filter((driver) => driver.driverId !== option.driverId) : [...selected, option]);

  return (
    <fieldset className="flex flex-col gap-3" disabled={disabled}>
      <legend className="mb-1.5 text-sm font-medium text-foreground">{labels.label}</legend>
      <SelectedDrivers drivers={selected} onRemove={toggle} onClear={() => onChange([])} disabled={disabled} />
      <div className="flex flex-wrap items-center justify-between gap-3">
        <SearchInput label={labels.searchLabel} placeholder={labels.searchPlaceholder} value={text} onSearch={setText} />
        {open.length > 0 && (
          <button type="button" onClick={() => onChange([...selected, ...open])} className="text-xs font-medium text-primary hover:underline">
            {labels.selectAll}
          </button>
        )}
      </div>
      {drivers.isError && <Alert>{labels.loadError}</Alert>}
      {drivers.isPending && <Spinner />}
      {drivers.isSuccess && items.length === 0 && <p className="text-sm text-muted">{labels.empty}</p>}
      {items.length > 0 && (
        <ul aria-label={labels.label} className="flex max-h-64 flex-col overflow-y-auto rounded-control border border-border">
          {items.map((option) => (
            <DriverOptionRow key={option.driverId} option={option} checked={chosen.has(option.driverId)} onToggle={() => toggle(option)} disabled={disabled} />
          ))}
        </ul>
      )}
      {error && <p className="text-xs text-danger">{error}</p>}
    </fieldset>
  );
}
