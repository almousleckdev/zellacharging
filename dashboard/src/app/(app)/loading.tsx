import { PageSkeleton } from "@/shared/ui/PageSkeleton";

export default function Loading() {
  return (
    <div role="status" aria-busy="true" className="mx-auto max-w-6xl">
      <PageSkeleton />
    </div>
  );
}
