"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCustomerSupport } from "../api/useCustomerSupport";
import { NdzelaDriverDetails } from "./NdzelaDriverDetails";
import type { CustomerDetail } from "../schemas";

export function NdzelaCard({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { profile } = useSession();
  const support = useCustomerSupport(customer.country, customer.id);
  const [confirming, setConfirming] = useState(false);
  const labels = t.customers.support.ndzela;
  const { linked, idTag } = customer.ndzela;
  const canManage = profile.permissions.includes("CUSTOMERS_MANAGE");
  const name = `${customer.firstName} ${customer.lastName}`;

  const confirm = () =>
    support.mutate(
      { kind: linked ? "unlink" : "link" },
      {
        onSuccess: () => notify("success", linked ? labels.unlinked : labels.linkedDone),
        onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
        onSettled: () => setConfirming(false),
      },
    );

  return (
    <Card
      title={labels.title}
      action={canManage && <Button variant={linked ? "secondary" : "primary"} onClick={() => setConfirming(true)}>{linked ? labels.unlink : labels.link}</Button>}
    >
      <div className="flex flex-col gap-1 text-sm">
        <p>{linked ? labels.linked : labels.notLinked}</p>
        {idTag && <p className="text-muted">{`${labels.idTag}: ${idTag}`}</p>}
      </div>
      {linked && <NdzelaDriverDetails customer={customer} />}
      <ConfirmDialog
        open={confirming}
        title={linked ? labels.unlinkTitle : labels.linkTitle}
        message={interpolate(linked ? labels.unlinkMessage : labels.linkMessage, { name })}
        confirmLabel={linked ? labels.unlink : labels.link}
        cancelLabel={t.dialogs.cancel}
        loading={support.isPending}
        onConfirm={confirm}
        onCancel={() => setConfirming(false)}
      />
    </Card>
  );
}
