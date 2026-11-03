import { cn } from "@/shared/lib/cn";
import { Icon } from "./Icon";
import type { IconName } from "./Icon";
import { Sparkline } from "./Sparkline";
import { TrendPill } from "./TrendPill";
import type { Trend } from "./TrendPill";

type MetricCardProps = {
  label: string;
  value: string;
  icon?: IconName;
  hint?: string;
  trend?: Trend;
  spark?: number[];
  compact?: boolean;
};

export function MetricCard({ label, value, icon, hint, trend, spark, compact = false }: MetricCardProps) {
  return (
    <article className="flex min-w-0 flex-col gap-3 rounded-card border border-border bg-background p-5 shadow-card">
      <header className="flex items-center gap-2.5">
        {icon && (
          <span className="flex size-9 shrink-0 items-center justify-center rounded-control bg-primary-soft text-primary-text">
            <Icon name={icon} className="size-[18px]" />
          </span>
        )}
        <h3 className="truncate text-sm font-medium text-muted">{label}</h3>
      </header>
      <div className="flex flex-wrap items-end justify-between gap-x-2 gap-y-1">
        <p className={cn("figure min-w-0 font-semibold text-foreground [overflow-wrap:anywhere]", compact ? "text-lg" : "text-xl sm:text-2xl")}>{value}</p>
        {spark && spark.length > 1 && <Sparkline values={spark} color="var(--color-primary)" className="h-8 w-16 shrink-0" />}
      </div>
      {trend && <TrendPill trend={trend} />}
      {hint && <p className="text-xs text-subtle">{hint}</p>}
    </article>
  );
}
