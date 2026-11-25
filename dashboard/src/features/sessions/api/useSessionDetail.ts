"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { sessionDetailSchema } from "../schemas";
import { LIVE_REFRESH_MS } from "./liveRefresh";

export function useSessionDetail(country: string, id: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "session", id),
    queryFn: () => adminFetch(`sessions/${encodeURIComponent(country)}/${encodeURIComponent(id)}`, sessionDetailSchema),
    refetchInterval: LIVE_REFRESH_MS,
  });
}
