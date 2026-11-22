"use client";

import { useState } from "react";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import type { AgreementTerms, DriverOption } from "../schemas";
import { useAgreementMutations } from "./useAgreementMutations";

export type CreateFailure = { driver: DriverOption; message: string };

export function useCreateMany() {
  const { t } = useI18n();
  const { create } = useAgreementMutations();
  const [progress, setProgress] = useState<{ done: number; total: number } | null>(null);
  const [added, setAdded] = useState(0);
  const [failures, setFailures] = useState<CreateFailure[]>([]);

  async function run(drivers: DriverOption[], terms: AgreementTerms): Promise<CreateFailure[]> {
    const failed: CreateFailure[] = [];
    let done = 0;
    let created = 0;
    setProgress({ done, total: drivers.length });
    for (const driver of drivers) {
      try {
        await create.mutateAsync({ customerId: driver.customerId ?? "", ...terms });
        created += 1;
      } catch (error) {
        failed.push({ driver, message: errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN") });
      }
      done += 1;
      setProgress({ done, total: drivers.length });
    }
    setAdded((before) => before + created);
    setFailures(failed);
    setProgress(null);
    return failed;
  }

  return { run, progress, added, failures, running: progress !== null };
}
