import { MetricBand } from "./MetricBand";
import { MetricCardSkeleton } from "./MetricCardSkeleton";
import { Skeleton } from "./Skeleton";

function TableRowSkeleton() {
  return (
    <div className="grid grid-cols-[2fr_1fr_1fr_1fr] items-center gap-6 border-t border-border px-5 py-4">
      <Skeleton className="h-3.5 w-3/4" />
      <Skeleton className="h-3.5 w-2/3" />
      <Skeleton className="h-3.5 w-1/2" />
      <Skeleton className="h-6 w-16 rounded-full" />
    </div>
  );
}

export function PageSkeleton() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-8">
      <div className="flex flex-col gap-2.5">
        <Skeleton className="h-7 w-64" />
        <Skeleton className="h-4 w-80 max-w-full" />
      </div>
      <MetricBand>
        {Array.from({ length: 4 }, (_, index) => (
          <MetricCardSkeleton key={index} />
        ))}
      </MetricBand>
      <div className="overflow-hidden rounded-card border border-border bg-background">
        <div className="flex items-center justify-between px-5 py-4">
          <Skeleton className="h-4 w-40" />
          <Skeleton className="h-8 w-28" />
        </div>
        {Array.from({ length: 6 }, (_, index) => (
          <TableRowSkeleton key={index} />
        ))}
      </div>
    </div>
  );
}
