import { cn } from "@/shared/lib/cn";

type ChargeBarProps = {
  level: number;
  cells?: number;
  tone?: "brand" | "energy" | "onDark";
  className?: string;
  label?: string;
};

const FILLED = {
  brand: "bg-primary",
  energy: "bg-energy",
  onDark: "bg-primary",
};

const EMPTY = {
  brand: "bg-surface-strong",
  energy: "bg-surface-strong",
  onDark: "bg-ink-line",
};

export function ChargeBar({ level, cells = 20, tone = "brand", className, label }: ChargeBarProps) {
  const filled = Math.round(Math.min(Math.max(level, 0), 1) * cells);
  return (
    <div
      role={label ? "img" : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
      className={cn("flex h-3 items-stretch gap-[3px]", className)}
    >
      {Array.from({ length: cells }, (_, index) => (
        <span
          key={index}
          style={{ animationDelay: `${index * 18}ms` }}
          className={cn("charge-cell flex-1 rounded-[3px]", index < filled ? FILLED[tone] : EMPTY[tone])}
        />
      ))}
    </div>
  );
}
