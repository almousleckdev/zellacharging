import type { ReactNode } from "react";

type CardProps = {
  title: string;
  description?: string;
  children: ReactNode;
  action?: ReactNode;
};

export function Card({ title, description, children, action }: CardProps) {
  return (
    <section className="rounded-card border border-border bg-background shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
      <header className="flex items-start justify-between gap-4 px-6 pt-5">
        <div className="flex flex-col gap-1">
          <h2 className="font-display text-base font-semibold text-foreground">{title}</h2>
          {description && <p className="text-sm text-muted">{description}</p>}
        </div>
        {action}
      </header>
      <div className="p-6 pt-4">{children}</div>
    </section>
  );
}
