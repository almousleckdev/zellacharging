"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { ApiError } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";

export const MAX_AVATAR_BYTES = 1_000_000;
export const AVATAR_TYPES = ["image/jpeg", "image/png", "image/webp"];

async function send(method: "PUT" | "DELETE", file?: File): Promise<void> {
  let response: Response;
  try {
    response = await fetch("/api/admin/me/avatar", {
      method,
      credentials: "same-origin",
      headers: file ? { "Content-Type": file.type } : undefined,
      body: file,
    });
  } catch {
    throw new ApiError(0, "NETWORK");
  }
  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as { code?: string } | null;
    throw new ApiError(response.status, body?.code ?? "UNKNOWN");
  }
}

export function useAvatar() {
  const client = useQueryClient();
  const refresh = () => client.invalidateQueries({ queryKey: queryKeys.profile });

  const upload = useMutation({ mutationFn: (file: File) => send("PUT", file), onSuccess: refresh });
  const remove = useMutation({ mutationFn: () => send("DELETE"), onSuccess: refresh });

  return { upload, remove };
}
