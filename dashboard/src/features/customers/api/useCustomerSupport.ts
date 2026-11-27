"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

export type SupportChannel = "EMAIL" | "PHONE";
export type SupportDelivery = "EMAIL" | "WHATSAPP" | "SMS";

export type SupportAction =
  | { kind: "resend-code"; channel: SupportChannel; delivery: SupportDelivery }
  | { kind: "reset-attempts"; channel: SupportChannel }
  | { kind: "verify"; channel: SupportChannel }
  | { kind: "unlock" }
  | { kind: "contact"; channel: SupportChannel; value: string }
  | { kind: "name"; firstName: string; lastName: string }
  | { kind: "password"; password: string }
  | { kind: "link" }
  | { kind: "unlink" };

const JSON_HEADERS = { "Content-Type": "application/json" };

function request(action: SupportAction): { path: string; init: RequestInit } {
  switch (action.kind) {
    case "resend-code":
      return post("resend-code", { channel: action.channel, delivery: action.delivery });
    case "reset-attempts":
    case "verify":
      return post(action.kind, { channel: action.channel });
    case "unlock":
      return { path: "unlock", init: { method: "POST" } };
    case "contact":
      return { path: "contact", init: { method: "PUT", headers: JSON_HEADERS, body: JSON.stringify({ channel: action.channel, value: action.value }) } };
    case "name":
      return { path: "name", init: { method: "PUT", headers: JSON_HEADERS, body: JSON.stringify({ firstName: action.firstName, lastName: action.lastName }) } };
    case "password":
      return { path: "password", init: { method: "PUT", headers: JSON_HEADERS, body: JSON.stringify({ password: action.password }) } };
    case "link":
      return { path: "ndzela-link", init: { method: "PUT" } };
    case "unlink":
      return { path: "ndzela-link", init: { method: "DELETE" } };
  }
}

function post(path: string, body: object): { path: string; init: RequestInit } {
  return { path, init: { method: "POST", headers: JSON_HEADERS, body: JSON.stringify(body) } };
}

export function useCustomerSupport(country: string, id: string) {
  const client = useQueryClient();
  const base = `customers/${encodeURIComponent(country)}/${encodeURIComponent(id)}`;

  return useMutation({
    mutationFn: (action: SupportAction) => {
      const { path, init } = request(action);
      return adminFetch(`${base}/${path}`, z.null(), init);
    },
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin", "scoped"] }),
  });
}
