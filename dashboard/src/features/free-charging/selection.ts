import type { DriverOption } from "./schemas";

export type Selection = { adds: Record<string, DriverOption>; removes: Record<string, DriverOption> };

export const NO_SELECTION: Selection = { adds: {}, removes: {} };

export function canAdd(driver: DriverOption): boolean {
  return !driver.inProgram && driver.linked && driver.status === "ACTIVE";
}

export function canRemove(driver: DriverOption): boolean {
  return driver.inProgram && Boolean(driver.agreementId);
}

export function isToggleable(driver: DriverOption): boolean {
  return canAdd(driver) || canRemove(driver);
}

export function isChecked(driver: DriverOption, selection: Selection): boolean {
  return driver.inProgram ? !(driver.driverId in selection.removes) : driver.driverId in selection.adds;
}

function without<T>(record: Record<string, T>, key: string): Record<string, T> {
  return Object.fromEntries(Object.entries(record).filter(([id]) => id !== key));
}

export function toggle(driver: DriverOption, selection: Selection): Selection {
  const key = driver.driverId;
  if (driver.inProgram) {
    return { ...selection, removes: key in selection.removes ? without(selection.removes, key) : { ...selection.removes, [key]: driver } };
  }
  return { ...selection, adds: key in selection.adds ? without(selection.adds, key) : { ...selection.adds, [key]: driver } };
}

export function toggleAll(drivers: DriverOption[], selection: Selection, check: boolean): Selection {
  return drivers.filter(isToggleable).reduce((current, driver) => (isChecked(driver, current) === check ? current : toggle(driver, current)), selection);
}

export function pendingCounts(selection: Selection): { add: number; remove: number } {
  return { add: Object.keys(selection.adds).length, remove: Object.keys(selection.removes).length };
}
