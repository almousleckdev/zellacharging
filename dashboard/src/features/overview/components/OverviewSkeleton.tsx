import { MetricCardSkeleton } from "@/shared/ui/MetricCardSkeleton";
import { Skeleton } from "@/shared/ui/Skeleton";

export function OverviewSkeleton() {
  return (
    <div aria-hidden="true" className="flex flex-col gap-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 7 }, (_, index) => (
          <MetricCardSkeleton key={index} />
        ))}
      </div>
      <div className="overflow-hidden rounded-card border border-border bg-background">
        <div className="px-5 py-4">
          <Skeleton className="h-4 w-40" />
        </div>
        {Array.from({ length: 3 }, (_, index) => (
          <div key={index} className="grid grid-cols-[2fr_1fr_1fr_1fr_1fr_1fr] gap-6 border-t border-border px-5 py-4">
            {Array.from({ length: 6 }, (_, cell) => (
              <Skeleton key={cell} className="h-3.5" />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
