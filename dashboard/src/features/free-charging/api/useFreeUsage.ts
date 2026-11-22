"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { usagePageSchema } from "../schemas";

export const USAGE_PAGE_SIZE = 20;

export function useFreeUsage(page: number) {
  return useQuery({
    queryKey: ["admin", "free-charging", "usage", page],
    queryFn: () => adminFetch(`free-charging/usage?page=${page}&size=${USAGE_PAGE_SIZE}`, usagePageSchema),
    placeholderData: keepPreviousData,
  });
}
