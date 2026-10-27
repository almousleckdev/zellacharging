"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { customerListSchema } from "../schemas";

export const PAGE_SIZE = 20;
const COUNTRY_INDEX = 2;

export type CustomerFilters = {
  country: string;
  text: string;
  status: string;
  page: number;
};

export function useCustomers(filters: CustomerFilters) {
  const { country, text, status, page } = filters;
  const query = new URLSearchParams({ country, size: String(PAGE_SIZE), page: String(page) });
  if (text) {
    query.set("q", text);
  }
  if (status) {
    query.set("status", status);
  }

  return useQuery({
    queryKey: queryKeys.scoped(country, "customers", text, status, String(page)),
    queryFn: () => adminFetch(`customers?${query.toString()}`, customerListSchema),
    placeholderData: (previous, previousQuery) =>
      previousQuery?.queryKey[COUNTRY_INDEX] === country ? previous : undefined,
  });
}
