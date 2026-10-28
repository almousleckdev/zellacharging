"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { paymentListSchema } from "../schemas";

export const PAGE_SIZE = 20;
const COUNTRY_INDEX = 2;

export type PaymentFilters = {
  country: string;
  text: string;
  kind: string;
  state: string;
  page: number;
};

export function usePayments(filters: PaymentFilters) {
  const { country, text, kind, state, page } = filters;
  const query = new URLSearchParams({ country, size: String(PAGE_SIZE), page: String(page) });
  if (text) {
    query.set("q", text);
  }
  if (kind) {
    query.set("kind", kind);
  }
  if (state) {
    query.set("state", state);
  }

  return useQuery({
    queryKey: queryKeys.scoped(country, "payments", text, kind, state, String(page)),
    queryFn: () => adminFetch(`payments?${query.toString()}`, paymentListSchema),
    placeholderData: (previous, previousQuery) =>
      previousQuery?.queryKey[COUNTRY_INDEX] === country ? previous : undefined,
  });
}
