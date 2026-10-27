import { Skeleton } from "./Skeleton";

export function MetricCardSkeleton() {
  return (
    <div className="flex flex-col gap-3 rounded-card border border-border bg-background p-5">
      <div className="flex items-center justify-between">
        <Skeleton className="h-3.5 w-28" />
        <Skeleton className="size-8" />
      </div>
      <Skeleton className="h-8 w-36" />
      <Skeleton className="h-3 w-24" />
    </div>
  );
}
