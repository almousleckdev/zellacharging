import "server-only";
import { NextResponse } from "next/server";
import { z } from "zod";
import { postBackend } from "./backendClient";
import type { BackendResult } from "./backendClient";
import { relayFailure } from "./proxyAuthStep";
import { clientIpOf, guardMutation, jsonError, readJson } from "./requestGuards";

export async function proxyAuthAction<Input>(
  request: Request,
  path: string,
  input: z.ZodType<Input>,
): Promise<NextResponse> {
  const rejected = guardMutation(request);
  if (rejected) {
    return rejected;
  }
  const parsed = input.safeParse(await readJson(request));
  if (!parsed.success) {
    return jsonError(400, "UNKNOWN");
  }
  let result: BackendResult;
  try {
    result = await postBackend(path, parsed.data, clientIpOf(request));
  } catch {
    return jsonError(502, "NETWORK");
  }
  return result.status === 204 ? new NextResponse(null, { status: 204 }) : relayFailure(result);
}
