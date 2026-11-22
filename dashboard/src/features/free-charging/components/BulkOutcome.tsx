"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import type { CreateFailure } from "../api/useCreateMany";

type BulkOutcomeProps = { added: number; failures: CreateFailure[] };

export function BulkOutcome({ added, failures }: BulkOutcomeProps) {
  const { t } = useI18n();
  const labels = t.freeCharging.bulk;

  return (
    <div className="flex flex-col gap-3" role="status">
      {added > 0 && <p className="text-sm font-medium text-foreground">{added === 1 ? labels.addedOne : interpolate(labels.addedMany, { count: added })}</p>}
      {failures.length > 0 && (
        <Alert>
          <p className="font-medium">{labels.failedTitle}</p>
          <ul className="mt-1 list-disc pl-5">
            {failures.map((failure) => (
              <li key={failure.driver.driverId}>{`${failure.driver.fullName} : ${failure.message}`}</li>
            ))}
          </ul>
        </Alert>
      )}
    </div>
  );
}
