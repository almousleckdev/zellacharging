"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { driverPageSchema } from "../schemas";

export const DRIVER_PAGE_SIZE = 25;
export type Membership = "ALL" | "IN_PROGRAM" | "NOT_IN_PROGRAM";

export function useDriverPage(text: string, membership: Membership, page: number) {
  const query = new URLSearchParams({ membership, page: String(page), size: String(DRIVER_PAGE_SIZE) });
  if (text) {
    query.set("q", text);
  }
  return useQuery({
    queryKey: ["admin", "free-charging", "drivers", text, membership, page],
    queryFn: () => adminFetch(`free-charging/drivers?${query.toString()}`, driverPageSchema),
    placeholderData: (previous) => previous,
  });
}
