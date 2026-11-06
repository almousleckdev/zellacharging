import { useId } from "react";
import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

type CardProps = {
  title: string;
  description?: string;
  children: ReactNode;
  action?: ReactNode;
  flush?: boolean;
};

export function Card({ title, description, children, action, flush = false }: CardProps) {
  const titleId = useId();

  return (
    <section aria-labelledby={titleId} className="overflow-hidden rounded-card border border-border bg-background shadow-card">
      <header className="flex items-center justify-between gap-3 border-b border-border px-4 py-3">
        <div className="min-w-0">
          <h2 id={titleId} className="text-sm font-semibold text-foreground">
            {title}
          </h2>
          {description && <p className="text-xs text-muted">{description}</p>}
        </div>
        {action}
      </header>
      <div className={cn(!flush && "p-4")}>{children}</div>
    </section>
  );
}
