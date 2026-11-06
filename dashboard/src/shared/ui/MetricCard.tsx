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
    <article className="flex min-w-0 flex-col gap-1.5 border-b border-r border-border px-4 py-3.5">
      <header className="flex items-center justify-between gap-2">
        <h3 className="truncate text-[13px] text-muted">{label}</h3>
        {icon && <Icon name={icon} className="size-4 text-subtle" />}
      </header>
      <div className="flex flex-wrap items-end justify-between gap-x-2 gap-y-1">
        <p className={cn("figure min-w-0 font-display font-semibold leading-none text-foreground [overflow-wrap:break-word]", compact ? "text-lg" : "text-[1.65rem]")}>{value}</p>
        {spark && spark.length > 1 && <Sparkline values={spark} color="var(--color-primary)" className="h-7 w-16 shrink-0" />}
      </div>
      {trend && <TrendPill trend={trend} />}
      {hint && <p className="text-xs text-muted">{hint}</p>}
    </article>
  );
}
