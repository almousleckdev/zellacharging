"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { adminProfileSchema } from "./schemas";

const PROFILE_STALE_MS = 5 * 60_000;

export function useAdminProfile() {
  return useQuery({
    queryKey: queryKeys.profile,
    queryFn: () => adminFetch("me", adminProfileSchema),
    staleTime: PROFILE_STALE_MS,
  });
}
