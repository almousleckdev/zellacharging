"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "@/shared/ui/Icon";
import type { DriverOption } from "../schemas";

type SelectedDriversProps = {
  drivers: DriverOption[];
  onRemove: (driver: DriverOption) => void;
  onClear: () => void;
  disabled?: boolean;
};

export function SelectedDrivers({ drivers, onRemove, onClear, disabled }: SelectedDriversProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;
  if (drivers.length === 0) {
    return null;
  }

  return (
    <div className="flex flex-col gap-2 rounded-control border border-primary bg-surface p-3">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm font-medium text-foreground">{drivers.length === 1 ? labels.selectedOne : interpolate(labels.selectedMany, { count: drivers.length })}</p>
        <button type="button" onClick={onClear} disabled={disabled} className="text-xs font-medium text-primary hover:underline disabled:opacity-50">
          {labels.clearAll}
        </button>
      </div>
      <ul className="flex flex-wrap gap-2">
        {drivers.map((driver) => (
          <li key={driver.driverId} className="inline-flex items-center gap-1 rounded-full border border-border bg-background py-0.5 pl-3 pr-1 text-xs text-foreground">
            {driver.fullName}
            <button
              type="button"
              aria-label={interpolate(labels.remove, { name: driver.fullName })}
              onClick={() => onRemove(driver)}
              disabled={disabled}
              className="grid size-5 place-items-center rounded-full text-muted hover:bg-surface disabled:opacity-50"
            >
              <Icon name="close" className="size-3" />
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
