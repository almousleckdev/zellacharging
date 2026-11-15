"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

export type CreateCustomerRequest = {
  country: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
};

const createdSchema = z.object({ id: z.string() });

export function useCreateCustomer() {
  const client = useQueryClient();

  return useMutation({
    mutationFn: ({ country, ...body }: CreateCustomerRequest) =>
      adminFetch(`customers/${encodeURIComponent(country)}`, createdSchema, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...body, email: body.email || null, phone: body.phone || null }),
      }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin", "scoped"] }),
  });
}
