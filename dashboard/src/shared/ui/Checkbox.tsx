import { useId } from "react";
import type { ComponentProps } from "react";

type CheckboxProps = Omit<ComponentProps<"input">, "id" | "type"> & {
  label: string;
  hint?: string;
};

export function Checkbox({ label, hint, ...rest }: CheckboxProps) {
  const id = useId();

  return (
    <div className="flex items-start gap-2.5">
      <input id={id} type="checkbox" className="mt-0.5 size-4 shrink-0 cursor-pointer accent-primary" {...rest} />
      <label htmlFor={id} className="flex cursor-pointer flex-col text-sm text-foreground">
        {label}
        {hint && <span className="text-xs text-subtle">{hint}</span>}
      </label>
    </div>
  );
}
