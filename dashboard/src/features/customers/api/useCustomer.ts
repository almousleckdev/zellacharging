"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { customerDetailSchema } from "../schemas";

export function useCustomer(country: string, id: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "customer", id),
    queryFn: () => adminFetch(`customers/${encodeURIComponent(country)}/${encodeURIComponent(id)}`, customerDetailSchema),
  });
}
