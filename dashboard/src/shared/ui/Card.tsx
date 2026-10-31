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
      <header className="flex items-start justify-between gap-4 px-5 pt-5">
        <div className="flex flex-col gap-1">
          <h2 id={titleId} className="text-base font-semibold text-foreground">
            {title}
          </h2>
          {description && <p className="text-sm text-subtle">{description}</p>}
        </div>
        {action}
      </header>
      <div className={cn("pt-4", flush ? "pb-0" : "p-5 pt-4")}>{children}</div>
    </section>
  );
}
