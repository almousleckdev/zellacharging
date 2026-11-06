import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

const COLUMNS = {
  3: "sm:grid-cols-3",
  4: "sm:grid-cols-2 xl:grid-cols-4",
} as const;

type MetricBandProps = {
  children: ReactNode;
  columns?: keyof typeof COLUMNS;
};

export function MetricBand({ children, columns = 4 }: MetricBandProps) {
  return (
    <div className="overflow-hidden rounded-card border border-border bg-background">
      <div className={cn("-mb-px -mr-px grid grid-cols-1", COLUMNS[columns])}>{children}</div>
    </div>
  );
}
