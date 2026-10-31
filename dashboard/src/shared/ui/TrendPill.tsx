import { cn } from "@/shared/lib/cn";
import { Icon } from "./Icon";

export type Trend = { delta: number | null; label: string; newLabel: string };

export function TrendPill({ trend }: { trend: Trend }) {
  const up = trend.delta !== null && trend.delta > 0;
  const down = trend.delta !== null && trend.delta < 0;

  return (
    <p className="flex flex-wrap items-center gap-1.5 text-xs text-subtle">
      <span
        className={cn(
          "inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 font-medium",
          up && "bg-success-soft text-success",
          down && "bg-danger-soft text-danger",
          !up && !down && "bg-surface-strong text-muted",
        )}
      >
        {(up || down) && <Icon name={up ? "arrowUp" : "arrowDown"} className="size-3" />}
        {trend.delta === null ? trend.newLabel : `${Math.abs(trend.delta).toFixed(1)} %`}
      </span>
      {trend.label}
    </p>
  );
}
