"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { settingsSectionSchema } from "../schemas";

type Save = { section: string; values: Record<string, number | boolean> };

export function useSaveSettings() {
  const client = useQueryClient();

  return useMutation({
    mutationFn: ({ section, values }: Save) =>
      adminFetch(`settings/${encodeURIComponent(section)}`, settingsSectionSchema, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ values }),
      }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin"] }),
  });
}
