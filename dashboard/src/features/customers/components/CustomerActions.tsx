"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ActionMenu } from "@/shared/ui/ActionMenu";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { useToast } from "@/shared/ui/ToastProvider";
import { useCustomerModeration } from "../api/useCustomerModeration";
import type { ModerationAction } from "../api/useCustomerModeration";
import type { CustomerDetail } from "../schemas";

export function CustomerActions({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const router = useRouter();
  const { profile, scopedHref } = useSession();
  const moderation = useCustomerModeration(customer.country, customer.id);
  const [pending, setPending] = useState<ModerationAction | null>(null);
  const labels = t.customers.moderation;
  const name = `${customer.firstName} ${customer.lastName}`;

  if (!profile.permissions.includes("CUSTOMERS_MANAGE")) {
    return null;
  }

  const suspended = customer.status === "BLOCKED";
  const items = [
    suspended
      ? { label: labels.reactivate, onSelect: () => setPending("reactivate") }
      : { label: labels.suspend, onSelect: () => setPending("suspend") },
    { label: labels.delete, onSelect: () => setPending("delete"), danger: true },
  ];

  const confirm = () => {
    if (!pending) {
      return;
    }
    moderation.mutate(pending, {
      onSuccess: () => {
        notify("success", labels.done[pending]);
        setPending(null);
        if (pending === "delete") {
          router.replace(scopedHref("/customers"));
        }
      },
      onError: (error) => {
        notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN"));
        setPending(null);
      },
    });
  };

  return (
    <>
      <ActionMenu label={labels.menu} trigger={labels.menu} items={items} />
      <ConfirmDialog
        open={pending !== null}
        title={pending ? labels.titles[pending] : ""}
        message={pending ? interpolate(labels.messages[pending], { name }) : ""}
        confirmLabel={pending ? labels[pending] : ""}
        cancelLabel={t.dialogs.cancel}
        loading={moderation.isPending}
        onConfirm={confirm}
        onCancel={() => setPending(null)}
      />
    </>
  );
}
