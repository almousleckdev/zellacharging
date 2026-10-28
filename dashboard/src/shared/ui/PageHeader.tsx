import type { ReactNode } from "react";
import { Breadcrumbs } from "./Breadcrumbs";

type PageHeaderProps = {
  title: string;
  subtitle?: string;
  trail: string[];
  actions?: ReactNode;
};

export function PageHeader({ title, subtitle, trail, actions }: PageHeaderProps) {
  return (
    <header className="flex flex-col gap-4 pb-6">
      <Breadcrumbs trail={trail} />
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="font-display text-3xl font-semibold text-foreground">{title}</h1>
          {subtitle && <p className="text-sm text-muted">{subtitle}</p>}
        </div>
        {actions}
      </div>
    </header>
  );
}
