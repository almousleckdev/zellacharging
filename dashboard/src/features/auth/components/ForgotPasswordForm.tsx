"use client";

import Link from "next/link";
import { useState } from "react";
import type { SubmitEvent } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { TextField } from "@/shared/ui/TextField";
import { requestPasswordReset } from "../api/authClient";
import { usePasswordRecovery } from "../hooks/usePasswordRecovery";
import { forgotPasswordSchema } from "../schemas";
import { fieldErrorsOf } from "../validation";
import type { FieldErrors } from "../validation";
import { AuthNotice } from "./AuthNotice";

export function ForgotPasswordForm() {
  const { t } = useI18n();
  const labels = t.recovery;
  const recovery = usePasswordRecovery();
  const [email, setEmail] = useState("");
  const [errors, setErrors] = useState<FieldErrors<"email">>({});

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = forgotPasswordSchema.safeParse({ email });
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    void recovery.run(() => requestPasswordReset(parsed.data.email));
  }

  if (recovery.done) {
    return <AuthNotice title={labels.sentTitle} text={labels.sentText} linkHref="/login" linkLabel={labels.backToLogin} />;
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1.5">
        <h1 className="text-3xl font-semibold text-foreground">{labels.forgotTitle}</h1>
        <p className="text-sm text-muted">{labels.forgotSubtitle}</p>
      </div>
      {recovery.errorCode && <Alert>{errorMessage(t, recovery.errorCode)}</Alert>}
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
          disabled={recovery.pending}
          autoFocus
        />
        <Button type="submit" fullWidth loading={recovery.pending}>
          {recovery.pending ? labels.sending : labels.send}
        </Button>
      </form>
      <Link href="/login" className="text-center text-sm font-semibold text-primary-text hover:underline">
        {labels.backToLogin}
      </Link>
    </div>
  );
}
