"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { Button } from "@/shared/ui/Button";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { useToast } from "@/shared/ui/ToastProvider";
import { useStopCharge } from "../api/useStopCharge";
import { canStopCharge } from "../format";
import type { SessionItem } from "../schemas";

export function StopChargeButton({ session, compact = false }: { session: SessionItem; compact?: boolean }) {
  const { t } = useI18n();
  const { profile } = useSession();
  const { notify } = useToast();
  const stop = useStopCharge();
  const [asking, setAsking] = useState(false);
  const labels = t.sessions.stop;

  if (!profile.permissions.includes("SESSIONS_STOP") || !canStopCharge(session)) {
    return null;
  }

  const confirm = () =>
    stop.mutate(
      { country: session.country, id: session.id },
      {
        onSuccess: () => notify("success", labels.done),
        onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
        onSettled: () => setAsking(false),
      },
    );

  return (
    <span onClick={(event) => event.stopPropagation()}>
      <Button variant="danger" className={compact ? "h-8 px-3" : undefined} onClick={() => setAsking(true)}>
        {labels.action}
      </Button>
      <ConfirmDialog
        open={asking}
        title={labels.title}
        message={interpolate(labels.message, { station: `${session.stationId} #${session.connectorId}`, customer: session.customerName })}
        confirmLabel={labels.confirm}
        cancelLabel={t.dialogs.cancel}
        loading={stop.isPending}
        destructive
        onConfirm={confirm}
        onCancel={() => setAsking(false)}
      />
    </span>
  );
}
