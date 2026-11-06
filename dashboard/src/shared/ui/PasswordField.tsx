"use client";

import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { TextField } from "./TextField";
import type { TextFieldProps } from "./TextField";

export function PasswordField(props: Omit<TextFieldProps, "type" | "trailing">) {
  const { t } = useI18n();
  const [visible, setVisible] = useState(false);

  return (
    <TextField
      placeholder="••••••••"
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
          {visible ? <EyeOff aria-hidden="true" className="size-4" /> : <Eye aria-hidden="true" className="size-4" />}
        </button>
      }
    />
  );
}
