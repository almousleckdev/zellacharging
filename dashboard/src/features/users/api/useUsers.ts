"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { adminUserPageSchema } from "../schemas";

export const USERS_PAGE_SIZE = 10;
export const USERS_KEY = ["admin", "users"] as const;

export type UserFilters = { text: string; status: string; page: number };

export function useUsers(filters: UserFilters) {
  const query = new URLSearchParams({ size: String(USERS_PAGE_SIZE), page: String(filters.page) });
  if (filters.text) {
    query.set("q", filters.text);
  }
  if (filters.status) {
    query.set("status", filters.status);
  }

  return useQuery({
    queryKey: [...USERS_KEY, filters.text, filters.status, filters.page],
    queryFn: () => adminFetch(`users?${query.toString()}`, adminUserPageSchema),
    placeholderData: keepPreviousData,
  });
}
