"use client";

import { useState } from "react";
import type { CustomerItem } from "@/features/customers/schemas";
import { ApiError } from "@/shared/api/adminFetch";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Alert } from "@/shared/ui/Alert";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Spinner } from "@/shared/ui/Spinner";
import { useLinkedCustomers } from "../api/useLinkedCustomers";
import { SelectedCustomer } from "./SelectedCustomer";

type LinkedCustomerPickerProps = {
  selected: CustomerItem | null;
  onSelect: (customer: CustomerItem) => void;
  error?: string;
  disabled?: boolean;
};

export function LinkedCustomerPicker({ selected, onSelect, error, disabled }: LinkedCustomerPickerProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;
  const [text, setText] = useState("");
  const customers = useLinkedCustomers(text);
  const items = customers.data?.items ?? [];

  return (
    <fieldset className="flex flex-col gap-3" disabled={disabled}>
      <legend className="mb-1.5 text-sm font-medium text-foreground">{labels.label}</legend>
      {selected && <SelectedCustomer customer={selected} />}
      <SearchInput label={labels.searchLabel} placeholder={labels.searchPlaceholder} value={text} onSearch={setText} />
      {customers.isError && <Alert>{customers.error instanceof ApiError && customers.error.status === 403 ? labels.permissionRequired : labels.loadError}</Alert>}
      {customers.isPending && <Spinner />}
      {customers.isSuccess && items.length === 0 && <p className="text-sm text-muted">{labels.empty}</p>}
      {items.length > 0 && (
        <ul role="listbox" aria-label={labels.label} className="flex max-h-56 flex-col overflow-y-auto rounded-control border border-border">
          {items.map((customer) => (
            <li key={customer.id} role="presentation">
              <button
                type="button"
                role="option"
                aria-selected={customer.id === selected?.id}
                onClick={() => onSelect(customer)}
                className={cn(
                  "flex w-full items-center justify-between gap-3 px-3 py-2 text-left text-sm hover:bg-surface",
                  customer.id === selected?.id && "bg-surface font-medium",
                )}
              >
                <span className="text-foreground">{`${customer.firstName} ${customer.lastName}`}</span>
                <span className="text-muted">{customer.phone ?? labels.noPhone}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
      {error && <p className="text-xs text-danger">{error}</p>}
    </fieldset>
  );
}
