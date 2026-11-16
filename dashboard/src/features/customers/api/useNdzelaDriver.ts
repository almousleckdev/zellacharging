"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { ndzelaDriverSchema } from "../ndzelaDriverSchema";

export function useNdzelaDriver(country: string, id: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "customer", id, "ndzela-driver"),
    queryFn: () =>
      adminFetch(`customers/${encodeURIComponent(country)}/${encodeURIComponent(id)}/ndzela-driver`, ndzelaDriverSchema),
    retry: false,
  });
}
