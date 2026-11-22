"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { driverOptionListSchema } from "../schemas";

export const DRIVER_SEARCH_SIZE = 20;

export function useDriverSearch(text: string) {
  const query = new URLSearchParams({ size: String(DRIVER_SEARCH_SIZE) });
  if (text) {
    query.set("q", text);
  }
  return useQuery({
    queryKey: ["admin", "free-charging", "drivers", text],
    queryFn: () => adminFetch(`free-charging/drivers?${query.toString()}`, driverOptionListSchema),
    placeholderData: (previous) => previous,
  });
}
