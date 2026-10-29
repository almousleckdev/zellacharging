"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { seriesWindow } from "../seriesMath";
import { seriesSchema } from "../seriesSchema";

export function useSeries(country: string, days: number) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "series", String(days)),
    queryFn: () => {
      const { from, to } = seriesWindow(days);
      const query = new URLSearchParams({ country, from, to, bucket: "day" });
      return adminFetch(`series?${query.toString()}`, seriesSchema);
    },
    refetchInterval: 60_000,
  });
}
