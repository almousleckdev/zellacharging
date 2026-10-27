"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { acceptInvitation, AuthApiError, completeTotp, inspectInvitation } from "@/features/auth/api/authClient";
import type { LoginStep } from "@/features/auth/schemas";
import type { ErrorCode } from "@/shared/i18n/types";

type Phase = "checking" | "invalid" | "password" | "security";

type State = {
  phase: Phase;
  email: string | null;
  step: LoginStep | null;
  pending: boolean;
  errorCode: ErrorCode | null;
};

const INITIAL: State = { phase: "checking", email: null, step: null, pending: false, errorCode: null };

function toErrorCode(error: unknown): ErrorCode {
  return error instanceof AuthApiError ? error.code : "UNKNOWN";
}

export function useInvitation(token: string | null) {
  const router = useRouter();
  const [state, setState] = useState<State>(token ? INITIAL : { ...INITIAL, phase: "invalid" });

  useEffect(() => {
    if (!token) {
      return;
    }
    let cancelled = false;
    inspectInvitation(token)
      .then((email) => !cancelled && setState({ ...INITIAL, phase: "password", email }))
      .catch(() => !cancelled && setState({ ...INITIAL, phase: "invalid" }));
    return () => {
      cancelled = true;
    };
  }, [token]);

  const submitPassword = useCallback(
    async (password: string) => {
      if (!token) {
        return;
      }
      setState((current) => ({ ...current, pending: true, errorCode: null }));
      try {
        const step = await acceptInvitation(token, password);
        setState((current) => ({ ...current, phase: "security", step, pending: false }));
      } catch (error) {
        const code = toErrorCode(error);
        setState((current) => ({
          ...current,
          phase: code === "INVITATION_INVALID" ? "invalid" : current.phase,
          pending: false,
          errorCode: code,
        }));
      }
    },
    [token],
  );

  const submitCode = useCallback(
    async (code: string) => {
      if (!state.step) {
        return false;
      }
      setState((current) => ({ ...current, pending: true, errorCode: null }));
      try {
        await completeTotp(state.step.challengeToken, code);
        router.replace("/");
        router.refresh();
        return true;
      } catch (error) {
        setState((current) => ({ ...current, pending: false, errorCode: toErrorCode(error) }));
        return false;
      }
    },
    [router, state.step],
  );

  return { ...state, submitPassword, submitCode };
}
