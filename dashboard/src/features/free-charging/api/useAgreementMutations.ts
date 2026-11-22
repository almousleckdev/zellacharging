"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { agreementSchema } from "../schemas";
import type { AgreementTerms } from "../schemas";
import { AGREEMENTS_KEY } from "./useAgreements";

const JSON_HEADERS = { "Content-Type": "application/json" };
const BASE = "free-charging/agreements";

export function useAgreementMutations() {
  const client = useQueryClient();
  const refresh = () => client.invalidateQueries({ queryKey: AGREEMENTS_KEY });
  const transition = (action: string) => (id: string) => adminFetch(`${BASE}/${id}/${action}`, agreementSchema, { method: "POST" });

  const create = useMutation({
    mutationFn: (values: AgreementTerms & { customerId: string }) =>
      adminFetch(BASE, agreementSchema, { method: "POST", headers: JSON_HEADERS, body: JSON.stringify(values) }),
    onSuccess: refresh,
  });
  const update = useMutation({
    mutationFn: ({ id, ...terms }: AgreementTerms & { id: string }) =>
      adminFetch(`${BASE}/${id}`, agreementSchema, { method: "PUT", headers: JSON_HEADERS, body: JSON.stringify(terms) }),
    onSuccess: refresh,
  });
  const suspend = useMutation({ mutationFn: transition("suspend"), onSuccess: refresh });
  const resume = useMutation({ mutationFn: transition("resume"), onSuccess: refresh });
  const revoke = useMutation({ mutationFn: transition("revoke"), onSuccess: refresh });

  return { create, update, suspend, resume, revoke };
}
