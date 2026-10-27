import type { ReactNode } from "react";
import { Icon } from "./Icon";
import type { IconName } from "./Icon";

type EmptyStateProps = {
  icon: IconName;
  title: string;
  text: string;
  action?: ReactNode;
};

export function EmptyState({ icon, title, text, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-card border border-dashed border-border-strong bg-surface px-6 py-16 text-center">
      <span className="flex size-12 items-center justify-center rounded-full border border-border bg-background text-primary-text">
        <Icon name={icon} className="size-6" />
      </span>
      <div className="flex max-w-sm flex-col gap-1">
        <p className="text-base font-semibold text-foreground">{title}</p>
        <p className="text-sm text-muted">{text}</p>
      </div>
      {action}
    </div>
  );
}
