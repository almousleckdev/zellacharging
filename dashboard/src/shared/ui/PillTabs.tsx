"use client";

import { cn } from "@/shared/lib/cn";

type PillTabsProps<Value extends string | number> = {
  label: string;
  options: { value: Value; label: string }[];
  value: Value;
  onChange: (value: Value) => void;
};

export function PillTabs<Value extends string | number>({ label, options, value, onChange }: PillTabsProps<Value>) {
  return (
    <div role="group" aria-label={label} className="inline-flex max-w-full gap-1 overflow-x-auto rounded-control border border-border bg-background p-1 shadow-card">
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          aria-pressed={value === option.value}
          onClick={() => onChange(option.value)}
          className={cn(
            "h-8 whitespace-nowrap rounded-[0.5rem] px-3.5 text-sm font-medium transition-colors",
            value === option.value ? "bg-primary text-white" : "text-muted hover:bg-surface-strong hover:text-foreground",
          )}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}
