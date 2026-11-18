"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { useToast } from "@/shared/ui/ToastProvider";
import { useFreeAccess } from "../api/useFreeAccess";
import type { FreeAccessRequest } from "../api/useFreeAccess";
import type { CustomerDetail } from "../schemas";
import { GiveFreeAccessDialog } from "./GiveFreeAccessDialog";

export function FreeAccessCard({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { notify } = useToast();
  const { profile } = useSession();
  const { give, remove } = useFreeAccess(customer.country, customer.id);
  const [giving, setGiving] = useState(false);
  const [removing, setRemoving] = useState(false);
  const labels = t.customers.freeAccess;
  const grant = customer.freeAccess;
  const canManage = profile.permissions.includes("COMPLIMENTARY_MANAGE");

  if (!grant && !canManage) {
    return null;
  }

  const fail = (error: unknown) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN"));
  const submit = (request: FreeAccessRequest) =>
    give.mutate(request, {
      onSuccess: () => {
        notify("success", labels.given);
        setGiving(false);
      },
      onError: fail,
    });
  const confirmRemove = () =>
    remove.mutate(undefined, {
      onSuccess: () => {
        notify("success", labels.removed);
        setRemoving(false);
      },
      onError: (error) => {
        fail(error);
        setRemoving(false);
      },
    });

  return (
    <Card
      title={labels.title}
      action={
        canManage && (
          <Button variant={grant ? "secondary" : "primary"} onClick={() => (grant ? setRemoving(true) : setGiving(true))}>
            {grant ? labels.remove : labels.give}
          </Button>
        )
      }
    >
      <div className="flex flex-col gap-1 text-sm">
        <p>{grant ? labels.active : labels.inactive}</p>
        {grant && (
          <>
            <p className="text-muted">{`${labels.category}: ${labels.categories[grant.category]}`}</p>
            <p className="text-muted">{`${labels.reason}: ${grant.reason}`}</p>
            <p className="text-muted">{interpolate(labels.since, { date: format.date(grant.grantedAt) })}</p>
            {grant.expiresAt && <p className="text-muted">{interpolate(labels.until, { date: format.date(grant.expiresAt) })}</p>}
          </>
        )}
      </div>
      <GiveFreeAccessDialog open={giving} loading={give.isPending} onSubmit={submit} onClose={() => setGiving(false)} />
      <ConfirmDialog
        open={removing}
        title={labels.removeTitle}
        message={interpolate(labels.removeMessage, { name: `${customer.firstName} ${customer.lastName}` })}
        confirmLabel={labels.remove}
        cancelLabel={t.dialogs.cancel}
        loading={remove.isPending}
        onConfirm={confirmRemove}
        onCancel={() => setRemoving(false)}
      />
    </Card>
  );
}
