"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import { Checkbox } from "@/shared/ui/Checkbox";
import type { DriverOption } from "../schemas";

type DriverOptionRowProps = {
  option: DriverOption;
  checked: boolean;
  onToggle: () => void;
  disabled?: boolean;
};

export function DriverOptionRow({ option, checked, onToggle, disabled }: DriverOptionRowProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;
  const blocked = option.inProgram ? labels.alreadyIn : option.linked ? null : labels.notLinked;
  const car = option.fleetNumber ? `${option.fleetNumber}${option.plate ? ` · ${option.plate}` : ""}` : labels.noCar;

  return (
    <li className="flex items-start justify-between gap-3 border-b border-border px-3 py-2 last:border-b-0">
      <Checkbox
        label={option.fullName}
        hint={`${option.phone ?? labels.noPhone}${option.email ? ` · ${option.email}` : ""}`}
        checked={checked}
        onChange={onToggle}
        disabled={disabled || blocked !== null}
      />
      <div className="flex shrink-0 flex-col items-end gap-1">
        <span className="text-xs font-medium text-foreground">{car}</span>
        {blocked && <Badge tone="warning">{blocked}</Badge>}
      </div>
    </li>
  );
}
