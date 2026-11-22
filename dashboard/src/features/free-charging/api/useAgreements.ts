"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { agreementListSchema } from "../schemas";

export const AGREEMENTS_KEY = ["admin", "free-charging", "agreements"] as const;

export function useAgreements() {
  return useQuery({
    queryKey: AGREEMENTS_KEY,
    queryFn: () => adminFetch("free-charging/agreements", agreementListSchema),
  });
}
