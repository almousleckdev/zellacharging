"use client";

import { useQuery } from "@tanstack/react-query";
import type { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { filterKey, filterSearch } from "./filters";
import type { ListFilters } from "./filters";

const COUNTRY_INDEX = 2;

export function useLogList<Output>(resource: string, filters: ListFilters, schema: z.ZodType<Output>, refreshMs?: number) {
  return useQuery({
    queryKey: queryKeys.scoped(filters.country, resource, ...filterKey(filters)),
    queryFn: () => adminFetch(`${resource}?${filterSearch(filters).toString()}`, schema),
    refetchInterval: refreshMs ?? false,
    placeholderData: (previous, previousQuery) =>
      previousQuery?.queryKey[COUNTRY_INDEX] === filters.country ? previous : undefined,
  });
}
