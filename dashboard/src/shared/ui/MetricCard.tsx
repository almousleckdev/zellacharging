import type { ReactNode } from "react";
import { Icon } from "./Icon";
import type { IconName } from "./Icon";

type MetricCardProps = {
  label: string;
  icon: IconName;
  values: string[];
  hint?: string;
  badge?: ReactNode;
};

export function MetricCard({ label, icon, values, hint, badge }: MetricCardProps) {
  return (
    <article className="flex flex-col gap-4 rounded-card border border-border bg-background p-5 shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
      <header className="flex items-center gap-2.5">
        <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary-soft text-primary-text">
          <Icon name={icon} className="size-4" />
        </span>
        <h3 className="text-sm font-medium text-muted">{label}</h3>
      </header>
      <div className="flex flex-col">
        {values.map((value, index) => (
          <p
            key={value}
            className={index === 0 ? "figure text-3xl font-semibold text-foreground" : "text-sm font-medium text-muted"}
          >
            {value}
          </p>
        ))}
      </div>
      {(hint || badge) && (
        <footer className="flex flex-wrap items-center gap-2 text-xs text-subtle">
          {badge}
          {hint && <span>{hint}</span>}
        </footer>
      )}
    </article>
  );
}
