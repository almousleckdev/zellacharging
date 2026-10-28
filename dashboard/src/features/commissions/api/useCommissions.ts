"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { commissionsSchema } from "../schemas";

export function useCommissions(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "commissions"),
    queryFn: () => adminFetch(`commissions?country=${encodeURIComponent(country)}`, commissionsSchema),
  });
}
