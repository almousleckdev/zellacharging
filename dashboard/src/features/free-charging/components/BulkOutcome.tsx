"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import type { ChangeResult } from "../api/useApplyChanges";

export function BulkOutcome({ result }: { result: ChangeResult }) {
  const { t } = useI18n();
  const labels = t.freeCharging.bulk;

  return (
    <div className="flex flex-col gap-2" role="status">
      {result.added > 0 && <p className="text-sm font-medium text-foreground">{result.added === 1 ? labels.addedOne : interpolate(labels.addedMany, { count: result.added })}</p>}
      {result.removed > 0 && <p className="text-sm font-medium text-foreground">{result.removed === 1 ? labels.removedOne : interpolate(labels.removedMany, { count: result.removed })}</p>}
      {result.failures.length > 0 && (
        <Alert>
          <p className="font-medium">{labels.failedTitle}</p>
          <ul className="mt-1 list-disc pl-5">
            {result.failures.map((failure) => (
              <li key={failure.driver.driverId}>{`${failure.driver.fullName} : ${failure.message}`}</li>
            ))}
          </ul>
        </Alert>
      )}
    </div>
  );
}
