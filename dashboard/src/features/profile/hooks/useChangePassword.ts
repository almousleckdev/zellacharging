"use client";

import { useMutation } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";
import type { PasswordChange } from "../schemas";

export function useChangePassword() {
  return useMutation({
    mutationFn: (values: PasswordChange) =>
      adminFetch("me/password", z.null(), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ currentPassword: values.currentPassword, newPassword: values.newPassword }),
      }),
  });
}
