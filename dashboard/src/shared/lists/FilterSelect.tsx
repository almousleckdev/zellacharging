"use client";

import { Select } from "@/shared/ui/Select";
import type { SelectOption } from "@/shared/ui/Select";

export type FilterSelectConfig = {
  key: string;
  label: string;
  allLabel: string;
  options: SelectOption[];
};

type FilterSelectProps = {
  config: FilterSelectConfig;
  value: string;
  onChange: (value: string) => void;
};

export function FilterSelect({ config, value, onChange }: FilterSelectProps) {
  return (
    <div className="w-full sm:w-44">
      <Select
        label={config.label}
        hideLabel
        options={[{ value: "", label: config.allLabel }, ...config.options]}
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </div>
  );
}
