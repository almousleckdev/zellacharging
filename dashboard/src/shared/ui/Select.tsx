import { useId } from "react";
import type { ComponentProps } from "react";
import { cn } from "@/shared/lib/cn";
import { FIELD } from "./fieldStyles";
import { Icon } from "./Icon";

export type SelectOption = { value: string; label: string };

type SelectProps = Omit<ComponentProps<"select">, "id" | "children"> & {
  label: string;
  options: SelectOption[];
  hideLabel?: boolean;
};

export function Select({ label, options, hideLabel = false, className, ...rest }: SelectProps) {
  const id = useId();

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className={cn("text-sm font-medium text-foreground", hideLabel && "sr-only")}>
        {label}
      </label>
      <div className="relative">
        <select
          id={id}
          className={cn(
            "h-9 w-full cursor-pointer appearance-none pl-3 pr-9",
            FIELD,
            className,
          )}
          {...rest}
        >
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <Icon name="chevronDown" className="pointer-events-none absolute right-2.5 top-1/2 size-4 -translate-y-1/2 text-subtle" />
      </div>
    </div>
  );
}
