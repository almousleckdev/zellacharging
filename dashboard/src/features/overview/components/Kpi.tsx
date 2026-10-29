import { Icon } from "@/shared/ui/Icon";
import type { IconName } from "@/shared/ui/Icon";
import { cn } from "@/shared/lib/cn";

type KpiProps = {
  label: string;
  icon: IconName;
  value: string;
  hint: string;
  delta: number | null;
  deltaLabel: string;
  newLabel: string;
};

export function Kpi({ label, icon, value, hint, delta, deltaLabel, newLabel }: KpiProps) {
  const up = delta !== null && delta > 0;
  const down = delta !== null && delta < 0;

  return (
    <article className="flex flex-col gap-3 rounded-card border border-border bg-background p-5 shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
      <header className="flex items-center justify-between gap-2">
        <h3 className="text-sm font-medium text-muted">{label}</h3>
        <span className="flex size-8 items-center justify-center rounded-full bg-primary-soft text-primary-text">
          <Icon name={icon} className="size-4" />
        </span>
      </header>
      <p className="figure font-display text-2xl font-semibold text-foreground">{value}</p>
      <footer className="flex flex-col gap-1 text-xs">
        <p
          className={cn(
            "flex items-center gap-1 font-medium",
            up && "text-success",
            down && "text-danger",
            !up && !down && "text-subtle",
          )}
        >
          {(up || down) && <Icon name={up ? "arrowUp" : "arrowDown"} className="size-3.5" />}
          {delta === null ? newLabel : `${Math.abs(delta).toFixed(1)} %`}
          <span className="font-normal text-subtle">{deltaLabel}</span>
        </p>
        <p className="text-subtle">{hint}</p>
      </footer>
    </article>
  );
}
