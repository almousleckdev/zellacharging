"use client";

import { useState } from "react";
import type { SubmitEvent } from "react";
import { fieldErrorsOf } from "@/features/auth/validation";
import type { FieldErrors } from "@/features/auth/validation";
import type { AdminProfile } from "@/features/session/schemas";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useUpdateProfile } from "../hooks/useUpdateProfile";
import { joinPhone, splitPhone } from "../phone";
import { profileDetailsSchema } from "../schemas";
import { PhoneField } from "./PhoneField";

type Field = "firstName" | "lastName" | "phoneDigits";

type ProfileDetailsFormProps = {
  profile: AdminProfile;
  submitLabel?: string;
  onSaved?: () => void;
};

export function ProfileDetailsForm({ profile, submitLabel, onSaved }: ProfileDetailsFormProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const save = useUpdateProfile();
  const initialPhone = splitPhone(profile.phone);
  const [firstName, setFirstName] = useState(profile.firstName ?? "");
  const [lastName, setLastName] = useState(profile.lastName ?? "");
  const [phone, setPhone] = useState(initialPhone);
  const [errors, setErrors] = useState<FieldErrors<Field>>({});
  const labels = t.profile.details;
  const message = (field: Field) => (errors[field] ? t.validation[errors[field]] : undefined);

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = profileDetailsSchema.safeParse({ firstName, lastName, phoneDigits: phone.digits });
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    save.mutate(
      { firstName: parsed.data.firstName, lastName: parsed.data.lastName, phone: joinPhone(phone.dial, parsed.data.phoneDigits) },
      {
        onSuccess: () => {
          notify("success", labels.saved);
          onSaved?.();
        },
      },
    );
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
      {save.isError && save.error instanceof ApiError && <Alert>{errorMessage(t, save.error.code)}</Alert>}
      <div className="grid gap-4 sm:grid-cols-2">
        <TextField label={labels.firstName} autoComplete="given-name" value={firstName} onChange={(e) => setFirstName(e.target.value)} error={message("firstName")} disabled={save.isPending} />
        <TextField label={labels.lastName} autoComplete="family-name" value={lastName} onChange={(e) => setLastName(e.target.value)} error={message("lastName")} disabled={save.isPending} />
      </div>
      <PhoneField dial={phone.dial} digits={phone.digits} onChange={setPhone} error={message("phoneDigits")} disabled={save.isPending} />
      <div>
        <Button type="submit" loading={save.isPending}>
          {save.isPending ? labels.saving : (submitLabel ?? labels.save)}
        </Button>
      </div>
    </form>
  );
}
