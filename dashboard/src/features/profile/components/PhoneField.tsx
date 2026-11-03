"use client";

import { useId } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { DIAL_CODES } from "../phone";

type PhoneFieldProps = {
  dial: string;
  digits: string;
  error?: string;
  disabled: boolean;
  onChange: (value: { dial: string; digits: string }) => void;
};

export function PhoneField({ dial, digits, error, disabled, onChange }: PhoneFieldProps) {
  const { t } = useI18n();
  const id = useId();
  const labels = t.profile.details;
  const messageId = `${id}-message`;

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-foreground">
        {labels.phone}
      </label>
      <div className="flex gap-2">
        <select
          aria-label={labels.dialCode}
          value={dial}
          disabled={disabled}
          onChange={(event) => onChange({ dial: event.target.value, digits })}
          className="h-10 rounded-control border border-transparent bg-surface-strong px-2 text-sm"
        >
          {DIAL_CODES.map((entry) => (
            <option key={entry.country} value={entry.dial}>
              {entry.name} {entry.dial}
            </option>
          ))}
        </select>
        <input
          id={id}
          type="tel"
          inputMode="numeric"
          autoComplete="tel-national"
          value={digits}
          disabled={disabled}
          aria-invalid={error ? true : undefined}
          aria-describedby={messageId}
          onChange={(event) => onChange({ dial, digits: event.target.value.replace(/\D/g, "").slice(0, 12) })}
          className={cn("h-10 min-w-0 flex-1 rounded-control border bg-surface-strong px-3 text-sm focus:bg-surface-focus", error ? "border-danger" : "border-transparent")}
        />
      </div>
      <p id={messageId} className={cn("text-xs", error ? "text-danger" : "text-subtle")}>
        {error ?? labels.phoneHint}
      </p>
    </div>
  );
}
