"use client";

import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { TextField } from "./TextField";
import type { TextFieldProps } from "./TextField";

function EyeIcon({ crossed }: { crossed: boolean }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="size-5">
      <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z" strokeLinejoin="round" />
      <circle cx="12" cy="12" r="3" />
      {crossed && <path d="M4 4l16 16" strokeLinecap="round" />}
    </svg>
  );
}

export function PasswordField(props: Omit<TextFieldProps, "type" | "trailing">) {
  const { t } = useI18n();
  const [visible, setVisible] = useState(false);

  return (
    <TextField
      {...props}
      type={visible ? "text" : "password"}
      trailing={
        <button
          type="button"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? t.login.hidePassword : t.login.showPassword}
          aria-pressed={visible}
          className="inline-flex size-8 items-center justify-center rounded-control text-subtle hover:bg-surface-strong hover:text-foreground"
        >
          <EyeIcon crossed={visible} />
        </button>
      }
    />
  );
}
