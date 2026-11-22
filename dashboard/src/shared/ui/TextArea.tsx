import { useId } from "react";
import type { ComponentProps } from "react";
import { cn } from "@/shared/lib/cn";
import { FIELD, FIELD_INVALID } from "./fieldStyles";

export type TextAreaProps = Omit<ComponentProps<"textarea">, "id"> & {
  label: string;
  error?: string;
  hint?: string;
};

export function TextArea({ label, error, hint, className, ...rest }: TextAreaProps) {
  const id = useId();
  const messageId = `${id}-message`;
  const message = error ?? hint;

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-foreground">
        {label}
      </label>
      <textarea
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={message ? messageId : undefined}
        className={cn("min-h-24 w-full resize-y px-3 py-2", FIELD, error && FIELD_INVALID, className)}
        {...rest}
      />
      {message && (
        <p id={messageId} className={cn("text-xs", error ? "text-danger" : "text-subtle")}>
          {message}
        </p>
      )}
    </div>
  );
}
