import { PageSkeleton } from "@/shared/ui/PageSkeleton";

export default function Loading() {
  return (
    <div role="status" aria-busy="true" className="flex flex-col gap-6">
      <PageSkeleton />
    </div>
  );
}
