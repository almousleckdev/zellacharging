import "server-only";
import { NextResponse } from "next/server";
import { z } from "zod";
import { postBackend } from "./backendClient";
import type { BackendResult } from "./backendClient";
import { clientIpOf, guardMutation, jsonError, readJson } from "./requestGuards";

const problemSchema = z.object({ code: z.string() });

const KNOWN_CODES = new Set([
  "INVALID_CREDENTIALS",
  "INVALID_TOTP_CODE",
  "ACCOUNT_LOCKED",
  "TOO_MANY_REQUESTS",
  "INVITATION_INVALID",
  "WEAK_PASSWORD",
]);

export function relayFailure(result: BackendResult): NextResponse {
  const code = problemSchema.safeParse(result.body);
  if (result.status >= 400 && result.status < 500 && code.success && KNOWN_CODES.has(code.data.code)) {
    return jsonError(result.status, code.data.code);
  }
  return jsonError(502, "UNKNOWN");
}

type Step<Input, Output> = {
  path: string;
  input: z.ZodType<Input>;
  output: z.ZodType<Output>;
  onSuccess: (data: Output) => Promise<NextResponse>;
};

export async function proxyAuthStep<Input, Output>(
  request: Request,
  step: Step<Input, Output>,
): Promise<NextResponse> {
  const rejected = guardMutation(request);
  if (rejected) {
    return rejected;
  }
  const input = step.input.safeParse(await readJson(request));
  if (!input.success) {
    return jsonError(400, "UNKNOWN");
  }
  let result: BackendResult;
  try {
    result = await postBackend(step.path, input.data, clientIpOf(request));
  } catch {
    return jsonError(502, "NETWORK");
  }
  if (result.status !== 200) {
    return relayFailure(result);
  }
  const output = step.output.safeParse(result.body);
  return output.success ? step.onSuccess(output.data) : jsonError(502, "UNKNOWN");
}
