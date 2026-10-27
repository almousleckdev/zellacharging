import { Skeleton } from "./Skeleton";

export function TableSkeleton({ rows = 8, columns = 5 }: { rows?: number; columns?: number }) {
  return (
    <div aria-hidden="true">
      {Array.from({ length: rows }, (_, row) => (
        <div
          key={row}
          className="grid items-center gap-6 border-t border-border px-5 py-4"
          style={{ gridTemplateColumns: `2fr repeat(${columns - 1}, 1fr)` }}
        >
          {Array.from({ length: columns }, (_, cell) => (
            <Skeleton key={cell} className={cell === columns - 1 ? "h-6 w-20 rounded-full" : "h-3.5"} />
          ))}
        </div>
      ))}
    </div>
  );
}
