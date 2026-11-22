"use client";

import { useQuery } from "@tanstack/react-query";
import { customerListSchema } from "@/features/customers/schemas";
import { useSession } from "@/features/session/SessionProvider";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";

export const LINKED_CUSTOMERS_PAGE_SIZE = 8;

export function useLinkedCustomers(text: string) {
  const { country } = useSession();
  const query = new URLSearchParams({ country, ndzelaDriver: "true", size: String(LINKED_CUSTOMERS_PAGE_SIZE), sort: "name,asc" });
  if (text) {
    query.set("q", text);
  }
  return useQuery({
    queryKey: queryKeys.scoped(country, "free-charging-customers", text),
    queryFn: () => adminFetch(`customers?${query.toString()}`, customerListSchema),
    placeholderData: (previous) => previous,
  });
}
