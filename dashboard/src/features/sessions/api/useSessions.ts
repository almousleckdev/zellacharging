"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { sessionListSchema } from "../schemas";

export const PAGE_SIZE = 20;
const COUNTRY_INDEX = 2;

export type SessionFilters = {
  country: string;
  text: string;
  state: string;
  page: number;
};

export function useSessions(filters: SessionFilters) {
  const { country, text, state, page } = filters;
  const query = new URLSearchParams({ country, size: String(PAGE_SIZE), page: String(page) });
  if (text) {
    query.set("q", text);
  }
  if (state) {
    query.set("state", state);
  }

  return useQuery({
    queryKey: queryKeys.scoped(country, "sessions", text, state, String(page)),
    queryFn: () => adminFetch(`sessions?${query.toString()}`, sessionListSchema),
    placeholderData: (previous, previousQuery) =>
      previousQuery?.queryKey[COUNTRY_INDEX] === country ? previous : undefined,
  });
}
