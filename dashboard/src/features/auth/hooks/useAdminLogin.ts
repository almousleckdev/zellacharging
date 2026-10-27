"use client";

import { useRouter } from "next/navigation";
import { useCallback, useState } from "react";
import { AuthApiError, completeTotp, requestLogin } from "../api/authClient";
import type { Credentials, LoginStep } from "../schemas";
import type { ErrorCode } from "@/shared/i18n/types";

export type LoginState = {
  step: LoginStep | null;
  pending: boolean;
  errorCode: ErrorCode | null;
};

const INITIAL: LoginState = { step: null, pending: false, errorCode: null };

function toErrorCode(error: unknown): ErrorCode {
  return error instanceof AuthApiError ? error.code : "UNKNOWN";
}

export function useAdminLogin(destination = "/") {
  const router = useRouter();
  const [state, setState] = useState<LoginState>(INITIAL);

  const submitCredentials = useCallback(async (credentials: Credentials) => {
    setState({ step: null, pending: true, errorCode: null });
    try {
      const step = await requestLogin(credentials);
      setState({ step, pending: false, errorCode: null });
    } catch (error) {
      setState({ step: null, pending: false, errorCode: toErrorCode(error) });
    }
  }, []);

  const submitCode = useCallback(
    async (code: string) => {
      if (!state.step) {
        return false;
      }
      setState((current) => ({ ...current, pending: true, errorCode: null }));
      try {
        await completeTotp(state.step.challengeToken, code);
        router.replace(destination);
        router.refresh();
        return true;
      } catch (error) {
        setState((current) => ({ ...current, pending: false, errorCode: toErrorCode(error) }));
        return false;
      }
    },
    [destination, router, state.step],
  );

  const restart = useCallback(() => setState(INITIAL), []);

  return { ...state, submitCredentials, submitCode, restart };
}
