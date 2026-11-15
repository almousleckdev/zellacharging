"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Badge } from "@/shared/ui/Badge";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { FactList } from "@/shared/ui/FactList";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCustomerSupport } from "../api/useCustomerSupport";
import type { SupportAction } from "../api/useCustomerSupport";
import type { CustomerDetail } from "../schemas";
import { ContactActionDialog } from "./ContactActionDialog";
import type { ContactMode } from "./ContactActionDialog";

const MODES: ContactMode[] = ["resend-code", "verify", "reset-attempts", "contact"];

export function AccessCard({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { notify } = useToast();
  const { profile } = useSession();
  const support = useCustomerSupport(customer.country, customer.id);
  const [mode, setMode] = useState<ContactMode | null>(null);
  const [unlocking, setUnlocking] = useState(false);
  const labels = t.customers.support;
  const detail = t.customers.detail;
  const { access } = customer;
  const canManage = profile.permissions.includes("CUSTOMERS_MANAGE");
  const done: Record<string, string> = labels.done;

  const run = (action: SupportAction) =>
    support.mutate(action, {
      onSuccess: () => notify("success", done[action.kind]),
      onError: (error) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN")),
      onSettled: () => {
        setMode(null);
        setUnlocking(false);
      },
    });

  const contact = (value: string | null, verified: boolean, pending: string | null) => (
    <span className="flex flex-wrap items-center justify-end gap-2">
      <span>{value ?? detail.none}</span>
      {value && <Badge tone={verified ? "success" : "warning"}>{verified ? detail.verified : detail.unverified}</Badge>}
      {pending && <Badge tone="info">{interpolate(labels.pending, { value: pending })}</Badge>}
    </span>
  );

  const locked = access.lockedUntil !== null;
  const facts = [
    { label: detail.email, value: contact(customer.email, customer.emailVerified, access.pendingEmail) },
    { label: detail.phone, value: contact(customer.phone, customer.phoneVerified, access.pendingPhone) },
    {
      label: labels.signIn,
      value: locked ? <Badge tone="danger">{interpolate(labels.lockedUntil, { date: format.date(access.lockedUntil as string) })}</Badge> : labels.signInOpen,
    },
  ];

  return (
    <Card title={labels.title} description={labels.description}>
      <FactList facts={facts} />
      {canManage && (
        <div className="mt-4 flex flex-wrap gap-2">
          {MODES.map((value) => (
            <Button key={value} variant="secondary" onClick={() => setMode(value)}>
              {labels.dialogs[value].title}
            </Button>
          ))}
          <Button variant="secondary" disabled={!locked} onClick={() => setUnlocking(true)}>
            {labels.unlock}
          </Button>
        </div>
      )}
      {mode && <ContactActionDialog mode={mode} customer={customer} loading={support.isPending} onSubmit={run} onClose={() => setMode(null)} />}
      <ConfirmDialog
        open={unlocking}
        title={labels.unlock}
        message={labels.unlockMessage}
        confirmLabel={labels.unlock}
        cancelLabel={t.dialogs.cancel}
        loading={support.isPending}
        onConfirm={() => run({ kind: "unlock" })}
        onCancel={() => setUnlocking(false)}
      />
    </Card>
  );
}
