import { Skeleton } from "./Skeleton";

export function MetricCardSkeleton() {
  return (
    <div className="flex flex-col gap-2 border-b border-r border-border px-4 py-3.5">
      <Skeleton className="h-3.5 w-28" />
      <Skeleton className="h-7 w-32" />
      <Skeleton className="h-3 w-24" />
    </div>
  );
}
