"use client";

import { useState } from "react";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Pagination } from "@/shared/ui/Pagination";
import { PillTabs } from "@/shared/ui/PillTabs";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Spinner } from "@/shared/ui/Spinner";
import { DRIVER_PAGE_SIZE, useDriverPage } from "../api/useDriverPage";
import type { Membership } from "../api/useDriverPage";
import { pendingCounts } from "../selection";
import type { Selection } from "../selection";
import { DriverTable } from "./DriverTable";

type DriverPickerProps = { selection: Selection; onChange: (selection: Selection) => void; onClear: () => void; disabled: boolean };

export function DriverPicker({ selection, onChange, onClear, disabled }: DriverPickerProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;
  const [text, setText] = useState("");
  const [membership, setMembership] = useState<Membership>("ALL");
  const [page, setPage] = useState(0);
  const drivers = useDriverPage(text, membership, page);
  const counts = pendingCounts(selection);
  const options = (Object.keys(labels.filters) as Membership[]).map((value) => ({ value, label: labels.filters[value] }));

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <SearchInput label={labels.searchLabel} placeholder={labels.searchPlaceholder} value={text} onSearch={(value) => { setText(value); setPage(0); }} />
        <PillTabs label={labels.filterLabel} options={options} value={membership} onChange={(value) => { setMembership(value); setPage(0); }} />
      </div>
      {drivers.isError && <Alert>{labels.loadError}</Alert>}
      {drivers.isPending && <Spinner />}
      {drivers.data && drivers.data.items.length === 0 && <p className="text-sm text-muted">{labels.empty}</p>}
      {drivers.data && drivers.data.items.length > 0 && (
        <div className={drivers.isPlaceholderData ? "opacity-60" : undefined}>
          <DriverTable drivers={drivers.data.items} selection={selection} onChange={onChange} disabled={disabled} />
          <Pagination page={page} size={DRIVER_PAGE_SIZE} total={drivers.data.total} onPage={setPage} />
        </div>
      )}
      <div className="flex flex-wrap items-center justify-between gap-2 text-sm" role="status">
        <p className="text-muted">{counts.add + counts.remove === 0 ? labels.pendingNone : interpolate(labels.pending, { add: counts.add, remove: counts.remove })}</p>
        {counts.add + counts.remove > 0 && (
          <button type="button" onClick={onClear} disabled={disabled} className="text-sm font-medium text-primary-text hover:underline">{labels.clearAll}</button>
        )}
      </div>
    </div>
  );
}
