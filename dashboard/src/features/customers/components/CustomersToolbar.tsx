"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Select } from "@/shared/ui/Select";

type CustomersToolbarProps = {
  text: string;
  status: string;
  onText: (value: string) => void;
  onStatus: (value: string) => void;
};

const STATUSES = ["ACTIVE", "BLOCKED", "PENDING_VERIFICATION", "PENDING_PHONE"] as const;

export function CustomersToolbar({ text, status, onText, onStatus }: CustomersToolbarProps) {
  const { t } = useI18n();
  const labels: Record<string, string> = t.customers.statuses;
  const options = [
    { value: "", label: t.customers.allStatuses },
    ...STATUSES.map((value) => ({ value, label: labels[value] })),
  ];

  return (
    <div className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
      <SearchInput
        label={t.customers.searchLabel}
        placeholder={t.customers.searchPlaceholder}
        value={text}
        onSearch={onText}
      />
      <div className="w-full sm:w-56">
        <Select label={t.customers.statusLabel} hideLabel options={options} value={status} onChange={(e) => onStatus(e.target.value)} />
      </div>
    </div>
  );
}
