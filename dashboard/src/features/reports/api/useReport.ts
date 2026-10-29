"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { rangeToInstants } from "@/shared/lib/dateRange";
import { seriesSchema } from "@/features/overview/seriesSchema";

export type ReportFilters = { country: string; from: string; to: string; bucket: string };

export function reportPath({ country, from, to, bucket }: ReportFilters): string {
  const query = new URLSearchParams({ country, bucket, ...rangeToInstants({ from, to }) });
  return `series?${query.toString()}`;
}

export function useReport(filters: ReportFilters) {
  return useQuery({
    queryKey: queryKeys.scoped(filters.country, "report", filters.from, filters.to, filters.bucket),
    queryFn: () => adminFetch(reportPath(filters), seriesSchema),
  });
}
