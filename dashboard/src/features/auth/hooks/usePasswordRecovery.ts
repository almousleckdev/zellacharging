"use client";

import { useCallback, useState } from "react";
import { AuthApiError } from "../api/authClient";
import type { ErrorCode } from "@/shared/i18n/types";

type State = {
  pending: boolean;
  done: boolean;
  errorCode: ErrorCode | null;
};

const INITIAL: State = { pending: false, done: false, errorCode: null };

export function usePasswordRecovery() {
  const [state, setState] = useState<State>(INITIAL);

  const run = useCallback(async (action: () => Promise<void>) => {
    setState({ pending: true, done: false, errorCode: null });
    try {
      await action();
      setState({ pending: false, done: true, errorCode: null });
    } catch (error) {
      setState({ pending: false, done: false, errorCode: error instanceof AuthApiError ? error.code : "UNKNOWN" });
    }
  }, []);

  return { ...state, run };
}
