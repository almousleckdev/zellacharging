"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { paymentDetailSchema } from "../schemas";

export function usePaymentDetail(country: string, kind: string, id: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "payment", kind, id),
    queryFn: () =>
      adminFetch(`payments/${encodeURIComponent(country)}/${encodeURIComponent(kind)}/${encodeURIComponent(id)}`, paymentDetailSchema),
  });
}
