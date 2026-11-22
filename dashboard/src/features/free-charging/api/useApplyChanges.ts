"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import type { AgreementTerms, DriverOption } from "../schemas";
import { useAgreementMutations } from "./useAgreementMutations";

export type ChangeFailure = { driver: DriverOption; message: string };
export type ChangeResult = { added: number; removed: number; failures: ChangeFailure[] };

export function useApplyChanges() {
  const { t } = useI18n();
  const { create, revoke } = useAgreementMutations();
  const [progress, setProgress] = useState<{ done: number; total: number } | null>(null);
  const [result, setResult] = useState<ChangeResult | null>(null);

  async function run(adds: DriverOption[], removes: DriverOption[], terms: AgreementTerms): Promise<ChangeFailure[]> {
    const total = adds.length + removes.length;
    const failures: ChangeFailure[] = [];
    let done = 0;
    let added = 0;
    let removed = 0;
    setProgress({ done, total });
    const attempt = async (driver: DriverOption, action: () => Promise<unknown>, onDone: () => void) => {
      try {
        await action();
        onDone();
      } catch (error) {
        failures.push({ driver, message: errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN") });
      }
      done += 1;
      setProgress({ done, total });
    };
    for (const driver of adds) {
      await attempt(driver, () => create.mutateAsync({ customerId: driver.customerId ?? "", ...terms }), () => (added += 1));
    }
    for (const driver of removes) {
      await attempt(driver, () => revoke.mutateAsync(driver.agreementId ?? ""), () => (removed += 1));
    }
    setResult((before) => ({ added: (before?.added ?? 0) + added, removed: (before?.removed ?? 0) + removed, failures }));
    setProgress(null);
    return failures;
  }

  return { run, progress, result, running: progress !== null };
}
