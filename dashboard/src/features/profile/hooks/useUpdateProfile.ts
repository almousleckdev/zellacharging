"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";

type ProfileChanges = { firstName: string; lastName: string; phone: string };

export function useUpdateProfile() {
  const client = useQueryClient();

  return useMutation({
    mutationFn: (changes: ProfileChanges) =>
      adminFetch("me/profile", z.null(), {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(changes),
      }),
    onSuccess: () => client.invalidateQueries({ queryKey: queryKeys.profile }),
  });
}
