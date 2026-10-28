import { cn } from "@/shared/lib/cn";

export type Segment = {
  amount: number;
  className: string;
};

type SegmentBarProps = {
  segments: Segment[];
  cells?: number;
  label: string;
  className?: string;
};

export function cellsFor(segments: Segment[], cells: number): number[] {
  const total = segments.reduce((sum, segment) => sum + Math.max(segment.amount, 0), 0);
  if (total <= 0) {
    return segments.map(() => 0);
  }
  const exact = segments.map((segment) => (Math.max(segment.amount, 0) / total) * cells);
  const counts = exact.map(Math.floor);
  let missing = cells - counts.reduce((sum, count) => sum + count, 0);
  const byRemainder = exact.map((value, index) => ({ index, rest: value - Math.floor(value) })).sort((a, b) => b.rest - a.rest);
  for (const { index } of byRemainder) {
    if (missing <= 0) {
      break;
    }
    if (segments[index].amount > 0) {
      counts[index] += 1;
      missing -= 1;
    }
  }
  return counts;
}

export function SegmentBar({ segments, cells = 40, label, className }: SegmentBarProps) {
  const counts = cellsFor(segments, cells);
  const filled = counts.reduce((sum, count) => sum + count, 0);

  return (
    <div role="img" aria-label={label} className={cn("flex h-5 items-stretch gap-[3px]", className)}>
      {segments.flatMap((segment, segmentIndex) =>
        Array.from({ length: counts[segmentIndex] }, (_, cell) => (
          <span
            key={`${segmentIndex}-${cell}`}
            style={{ animationDelay: `${(counts.slice(0, segmentIndex).reduce((sum, count) => sum + count, 0) + cell) * 14}ms` }}
            className={cn("charge-cell flex-1 rounded-[3px]", segment.className)}
          />
        )),
      )}
      {Array.from({ length: Math.max(cells - filled, 0) }, (_, cell) => (
        <span key={`empty-${cell}`} className="flex-1 rounded-[3px] bg-surface-strong" />
      ))}
    </div>
  );
}
