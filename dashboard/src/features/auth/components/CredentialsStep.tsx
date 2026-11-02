"use client";

import Link from "next/link";
import { useState } from "react";
import type { SubmitEvent } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { PasswordField } from "@/shared/ui/PasswordField";
import { TextField } from "@/shared/ui/TextField";
import { credentialsSchema } from "../schemas";
import type { Credentials } from "../schemas";
import { fieldErrorsOf } from "../validation";
import type { FieldErrors } from "../validation";

type Props = {
  pending: boolean;
  onSubmit: (credentials: Credentials) => void;
};

export function CredentialsStep({ pending, onSubmit }: Props) {
  const { t } = useI18n();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<FieldErrors<"email" | "password">>({});

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = credentialsSchema.safeParse({ email, password });
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    onSubmit(parsed.data);
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">
      <TextField
        label={t.login.email}
        type="email"
        name="email"
        autoComplete="username"
        placeholder={t.login.emailPlaceholder}
        value={email}
        onChange={(event) => setEmail(event.target.value)}
        error={errors.email ? t.validation[errors.email] : undefined}
        disabled={pending}
        autoFocus
      />
      <PasswordField
        label={t.login.password}
        name="password"
        autoComplete="current-password"
        value={password}
        onChange={(event) => setPassword(event.target.value)}
        error={errors.password ? t.validation[errors.password] : undefined}
        disabled={pending}
      />
      <Link href="/forgot-password" className="-mt-2 self-end text-sm font-medium text-primary-text hover:underline">
        {t.login.forgot}
      </Link>
      <Button type="submit" fullWidth loading={pending}>
        {pending ? t.login.submitting : t.login.submit}
      </Button>
    </form>
  );
}
