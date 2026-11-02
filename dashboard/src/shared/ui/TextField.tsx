import { useId } from "react";
import type { ComponentProps, ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

export type TextFieldProps = Omit<ComponentProps<"input">, "id"> & {
  label: string;
  error?: string;
  hint?: string;
  trailing?: ReactNode;
};

export function TextField({ label, error, hint, trailing, className, ...rest }: TextFieldProps) {
  const id = useId();
  const messageId = `${id}-message`;
  const message = error ?? hint;

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-foreground">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          aria-invalid={error ? true : undefined}
          aria-describedby={message ? messageId : undefined}
          className={cn(
            "h-11 w-full rounded-control border bg-background px-3.5 text-sm text-foreground shadow-sm transition",
            "placeholder:text-subtle disabled:bg-surface disabled:text-subtle",
            "focus:outline-none focus-visible:outline-none focus:ring-4",
            error
              ? "border-danger focus:ring-danger/10"
              : "border-border-strong hover:border-subtle focus:border-foreground focus:ring-foreground/10",
            trailing ? "pr-11" : undefined,
            className,
          )}
          {...rest}
        />
        {trailing && <div className="absolute inset-y-0 right-0 flex items-center pr-1.5">{trailing}</div>}
      </div>
      {message && (
        <p id={messageId} className={cn("text-xs", error ? "text-danger" : "text-subtle")}>
          {message}
        </p>
      )}
    </div>
  );
}
