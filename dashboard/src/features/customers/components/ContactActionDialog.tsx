"use client";

import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { Select } from "@/shared/ui/Select";
import { TextField } from "@/shared/ui/TextField";
import type { SupportAction, SupportChannel, SupportDelivery } from "../api/useCustomerSupport";
import type { CustomerDetail } from "../schemas";

export type ContactMode = "resend-code" | "reset-attempts" | "verify" | "contact";

type ContactActionDialogProps = {
  mode: ContactMode;
  customer: CustomerDetail;
  loading: boolean;
  onSubmit: (action: SupportAction) => void;
  onClose: () => void;
};

function firstToFix(customer: CustomerDetail): SupportChannel {
  return customer.phone && !customer.phoneVerified ? "PHONE" : "EMAIL";
}

export function ContactActionDialog({ mode, customer, loading, onSubmit, onClose }: ContactActionDialogProps) {
  const { t } = useI18n();
  const labels = t.customers.support;
  const [channel, setChannel] = useState<SupportChannel>(firstToFix(customer));
  const [phoneDelivery, setPhoneDelivery] = useState<SupportDelivery>("WHATSAPP");
  const [value, setValue] = useState("");
  const delivery: SupportDelivery = channel === "EMAIL" ? "EMAIL" : phoneDelivery;
  const deliveries: Record<string, string> = labels.deliveries;

  const submit = () => {
    if (mode === "resend-code") {
      onSubmit({ kind: mode, channel, delivery });
    } else if (mode === "contact") {
      onSubmit({ kind: mode, channel, value: value.trim() });
    } else {
      onSubmit({ kind: mode, channel });
    }
  };

  return (
    <Modal
      open
      title={labels.dialogs[mode].title}
      description={labels.dialogs[mode].message}
      busy={loading}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={loading}>
            {t.dialogs.cancel}
          </Button>
          <Button onClick={submit} loading={loading} disabled={mode === "contact" && value.trim() === ""}>
            {labels.dialogs[mode].confirm}
          </Button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        <Select
          label={labels.channelLabel}
          value={channel}
          onChange={(event) => setChannel(event.target.value as SupportChannel)}
          options={[
            { value: "EMAIL", label: t.customers.detail.email },
            { value: "PHONE", label: t.customers.detail.phone },
          ]}
        />
        {mode === "resend-code" && channel === "PHONE" && (
          <Select
            label={labels.deliveryLabel}
            value={phoneDelivery}
            onChange={(event) => setPhoneDelivery(event.target.value as SupportDelivery)}
            options={["WHATSAPP", "SMS"].map((option) => ({ value: option, label: deliveries[option] }))}
          />
        )}
        {mode === "contact" && (
          <TextField
            label={labels.newValueLabel}
            hint={channel === "PHONE" ? labels.phoneHint : undefined}
            type={channel === "EMAIL" ? "email" : "tel"}
            value={value}
            onChange={(event) => setValue(event.target.value)}
          />
        )}
      </div>
    </Modal>
  );
}
