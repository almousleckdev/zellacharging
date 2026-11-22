"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import type { DriverOption } from "../schemas";
import { isChecked, isToggleable, toggle, toggleAll } from "../selection";
import type { Selection } from "../selection";
import { DriverGratuityBadge } from "./DriverGratuityBadge";

type DriverTableProps = { drivers: DriverOption[]; selection: Selection; onChange: (selection: Selection) => void; disabled: boolean };

function carOf(driver: DriverOption, empty: string): string {
  const parts = [driver.fleetNumber, driver.plate].filter(Boolean);
  return parts.length > 0 ? parts.join(" · ") : empty;
}

export function DriverTable({ drivers, selection, onChange, disabled }: DriverTableProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;
  const statuses: Record<string, string> = labels.driverStatuses;
  const toggleable = drivers.filter(isToggleable);
  const allChecked = toggleable.length > 0 && toggleable.every((driver) => isChecked(driver, selection));

  return (
    <div className="overflow-x-auto rounded-control border border-border">
      <table className="w-full min-w-[640px] text-left text-sm">
        <caption className="sr-only">{labels.label}</caption>
        <thead className="bg-surface-strong text-xs uppercase tracking-wide text-muted">
          <tr>
            <th scope="col" className="w-12 px-3 py-2">
              <input
                type="checkbox"
                aria-label={labels.selectPage}
                className="size-4 cursor-pointer accent-primary"
                checked={allChecked}
                disabled={disabled || toggleable.length === 0}
                onChange={() => onChange(toggleAll(drivers, selection, !allChecked))}
              />
            </th>
            <th scope="col" className="px-3 py-2">{labels.columns.driver}</th>
            <th scope="col" className="px-3 py-2">{labels.columns.car}</th>
            <th scope="col" className="px-3 py-2">{labels.columns.driverStatus}</th>
            <th scope="col" className="px-3 py-2">{labels.columns.gratuity}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {drivers.map((driver) => (
            <tr key={driver.driverId} className={isToggleable(driver) ? "hover:bg-surface-strong/60" : "text-muted"}>
              <td className="px-3 py-2.5">
                <input
                  type="checkbox"
                  aria-label={interpolate(labels.rowLabel, { name: driver.fullName })}
                  className="size-4 cursor-pointer accent-primary disabled:cursor-not-allowed"
                  checked={isChecked(driver, selection)}
                  disabled={disabled || !isToggleable(driver)}
                  onChange={() => onChange(toggle(driver, selection))}
                />
              </td>
              <td className="px-3 py-2.5">
                <p className="font-medium text-foreground">{driver.fullName}</p>
                <p className="text-xs text-subtle">{driver.phone ?? labels.noPhone}{driver.email ? ` · ${driver.email}` : ""}</p>
              </td>
              <td className="px-3 py-2.5">{carOf(driver, labels.noCar)}</td>
              <td className="px-3 py-2.5">{statuses[driver.status] ?? driver.status}</td>
              <td className="px-3 py-2.5"><DriverGratuityBadge driver={driver} selection={selection} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
