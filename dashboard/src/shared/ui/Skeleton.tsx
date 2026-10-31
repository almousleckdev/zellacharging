import { cn } from "@/shared/lib/cn";

type SkeletonProps = {
  className?: string;
};

export function Skeleton({ className }: SkeletonProps) {
  return <div aria-hidden="true" className={cn("skeleton", className)} />;
}
