"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import type { DriverOption } from "../schemas";
import { canAdd, isChecked } from "../selection";
import type { Selection } from "../selection";

export function DriverGratuityBadge({ driver, selection }: { driver: DriverOption; selection: Selection }) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker.gratuity;
  const checked = isChecked(driver, selection);

  if (driver.inProgram) {
    if (!checked) {
      return <Badge tone="danger">{labels.willRemove}</Badge>;
    }
    return driver.agreementStatus === "SUSPENDED" ? <Badge tone="warning">{labels.suspended}</Badge> : <Badge tone="success">{labels.inProgram}</Badge>;
  }
  if (checked) {
    return <Badge tone="info">{labels.willAdd}</Badge>;
  }
  if (!driver.linked) {
    return <Badge tone="warning">{labels.notLinked}</Badge>;
  }
  return <Badge tone="neutral">{canAdd(driver) ? labels.out : labels.notActive}</Badge>;
}
