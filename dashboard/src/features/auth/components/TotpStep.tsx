"use client";

import { useRef, useState } from "react";
import type { SubmitEvent } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { TextField } from "@/shared/ui/TextField";
import { totpCodeSchema } from "../schemas";
import type { LoginStep } from "../schemas";
import { EnrollmentPanel } from "./EnrollmentPanel";

type Props = {
  step: LoginStep;
  pending: boolean;
  onSubmit: (code: string) => Promise<boolean>;
  onBack?: () => void;
};

const CODE_LENGTH = 6;

function onlyDigits(value: string): string {
  return value.replace(/\D/g, "").slice(0, CODE_LENGTH);
}

export function TotpStep({ step, pending, onSubmit, onBack }: Props) {
  const { t } = useI18n();
  const input = useRef<HTMLInputElement>(null);
  const [code, setCode] = useState("");
  const [invalid, setInvalid] = useState(false);

  async function submit(value: string) {
    if (!totpCodeSchema.safeParse(value).success) {
      setInvalid(true);
      return;
    }
    setInvalid(false);
    const accepted = await onSubmit(value);
    if (!accepted) {
      setCode("");
      input.current?.focus();
    }
  }

  function handleChange(raw: string) {
    const value = onlyDigits(raw);
    setCode(value);
    setInvalid(false);
    if (value.length === CODE_LENGTH && !pending) {
      void submit(value);
    }
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    void submit(code);
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">
      {step.stage === "TOTP_ENROLLMENT" && (
        <EnrollmentPanel secret={step.secret} provisioningUri={step.provisioningUri} />
      )}
      <TextField
        ref={input}
        label={t.totp.code}
        name="code"
        inputMode="numeric"
        autoComplete="one-time-code"
        pattern="[0-9]*"
        maxLength={CODE_LENGTH}
        placeholder="000000"
        value={code}
        onChange={(event) => handleChange(event.target.value)}
        error={invalid ? t.validation.codeInvalid : undefined}
        disabled={pending}
        className="text-center font-mono text-lg tracking-[0.5em]"
        autoFocus
      />
      <div className="flex flex-col gap-2">
        <Button type="submit" fullWidth loading={pending}>
          {pending ? t.totp.verifying : t.totp.verify}
        </Button>
        {onBack && (
          <Button variant="ghost" fullWidth onClick={onBack} disabled={pending}>
            {t.totp.back}
          </Button>
        )}
      </div>
    </form>
  );
}
