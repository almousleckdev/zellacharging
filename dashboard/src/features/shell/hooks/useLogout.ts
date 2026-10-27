"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useToast } from "@/shared/ui/ToastProvider";

async function requestLogout(): Promise<void> {
  const response = await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
  if (!response.ok) {
    throw new Error("logout failed");
  }
}

export function useLogout() {
  const { t } = useI18n();
  const { notify } = useToast();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: requestLogout,
    onSuccess: () => {
      queryClient.clear();
      window.location.replace("/login");
    },
    onError: () => notify("error", t.errors.NETWORK),
  });
}
