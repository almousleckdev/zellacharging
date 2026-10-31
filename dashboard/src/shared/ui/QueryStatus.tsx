"use client";

import { ErrorState } from "./ErrorState";
import { Skeleton } from "./Skeleton";

type QueryStatusProps = {
  pending: boolean;
  failed: boolean;
  loadError: string;
  onRetry: () => void;
};

export function QueryStatus({ pending, failed, loadError, onRetry }: QueryStatusProps) {
  return (
    <>
      {pending && <Skeleton className="h-48" />}
      {failed && <ErrorState message={loadError} onRetry={onRetry} />}
    </>
  );
}
