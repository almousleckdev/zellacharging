"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";
import { adminUserSchema, emailResultSchema, invitationResultSchema } from "../schemas";
import type { AccessDefinition } from "../schemas";
import { USERS_KEY } from "./useUsers";

const JSON_HEADERS = { "Content-Type": "application/json" };

export function useUserMutations() {
  const client = useQueryClient();
  const refresh = () => client.invalidateQueries({ queryKey: USERS_KEY });

  const invite = useMutation({
    mutationFn: (values: AccessDefinition & { email: string }) =>
      adminFetch("users", invitationResultSchema, { method: "POST", headers: JSON_HEADERS, body: JSON.stringify(values) }),
    onSuccess: refresh,
  });
  const update = useMutation({
    mutationFn: ({ id, ...values }: AccessDefinition & { id: string }) =>
      adminFetch(`users/${id}`, adminUserSchema, { method: "PUT", headers: JSON_HEADERS, body: JSON.stringify(values) }),
    onSuccess: refresh,
  });
  const resend = useMutation({
    mutationFn: (id: string) => adminFetch(`users/${id}/invitation`, emailResultSchema, { method: "POST" }),
  });
  const disable = useMutation({
    mutationFn: (id: string) => adminFetch(`users/${id}/disable`, adminUserSchema, { method: "POST" }),
    onSuccess: refresh,
  });
  const enable = useMutation({
    mutationFn: (id: string) => adminFetch(`users/${id}/enable`, adminUserSchema, { method: "POST" }),
    onSuccess: refresh,
  });
  const remove = useMutation({
    mutationFn: (id: string) => adminFetch(`users/${id}`, z.null(), { method: "DELETE" }),
    onSuccess: refresh,
  });

  return { invite, update, resend, disable, enable, remove };
}
