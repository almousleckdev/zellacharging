"use client";

import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ListPanel } from "@/shared/ui/ListPanel";
import { useToast } from "@/shared/ui/ToastProvider";
import { useAgreementMutations } from "../api/useAgreementMutations";
import { useAgreements } from "../api/useAgreements";
import type { FreeChargingDialog } from "../dialog";
import type { Agreement } from "../schemas";
import { AgreementFormDialog } from "./AgreementFormDialog";
import { AgreementRevokeConfirm } from "./AgreementRevokeConfirm";
import { AgreementsTable } from "./AgreementsTable";
import type { AgreementAction } from "./AgreementsTable";

export function AgreementsTab({ dialog, onDialog }: { dialog: FreeChargingDialog; onDialog: (dialog: FreeChargingDialog) => void }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const agreements = useAgreements();
  const { suspend, resume } = useAgreementMutations();
  const labels = t.freeCharging;
  const items = agreements.data ?? [];

  function toggle(action: "suspend" | "resume", agreement: Agreement) {
    const mutation = action === "suspend" ? suspend : resume;
    mutation.mutate(agreement.id, {
      onSuccess: () => notify("success", action === "suspend" ? labels.toasts.suspended : labels.toasts.resumed),
      onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
    });
  }

  function handle(action: AgreementAction, agreement: Agreement) {
    if (action === "edit") {
      onDialog({ kind: "form", agreement });
    } else if (action === "revoke") {
      onDialog({ kind: "revoke", agreement });
    } else {
      toggle(action, agreement);
    }
  }

  return (
    <>
      <ListPanel
        pending={agreements.isPending}
        failed={agreements.isError}
        onRetry={() => void agreements.refetch()}
        loadError={labels.loadError}
        empty={{ icon: "bolt", title: labels.emptyTitle, text: labels.emptyText }}
        isEmpty={agreements.isSuccess && items.length === 0}
      >
        {items.length > 0 && <AgreementsTable items={items} onAction={handle} />}
      </ListPanel>
      {dialog?.kind === "form" && <AgreementFormDialog key={dialog.agreement?.id ?? "new"} agreement={dialog.agreement} onClose={() => onDialog(null)} />}
      {dialog?.kind === "revoke" && <AgreementRevokeConfirm agreement={dialog.agreement} onClose={() => onDialog(null)} />}
    </>
  );
}
