import "server-only";
import { NextResponse } from "next/server";
import { requestBackend } from "./backendClient";
import type { BackendResult } from "./backendClient";
import { clientIpOf, guardMutation, jsonError, readJson } from "./requestGuards";
import { callWithSession } from "./sessionCall";

const SEGMENT = /^[A-Za-z0-9_-]+$/;
const READ_METHODS = new Set(["GET", "HEAD"]);

function backendPath(segments: string[], search: string): string | null {
  const valid = segments.length > 0 && segments[0] !== "auth" && segments.every((part) => SEGMENT.test(part));
  return valid ? `/v1/admin/${segments.join("/")}${search}` : null;
}

function relay(result: BackendResult): NextResponse {
  if (result.status === 204 || result.body === null) {
    return new NextResponse(null, { status: result.status });
  }
  if (result.status >= 400) {
    const code = (result.body as { code?: unknown }).code;
    return jsonError(result.status, typeof code === "string" ? code : "UNKNOWN");
  }
  return NextResponse.json(result.body, { status: result.status });
}

export async function proxyAdminRequest(request: Request, segments: string[]): Promise<NextResponse> {
  const readOnly = READ_METHODS.has(request.method);
  if (!readOnly) {
    const rejected = guardMutation(request);
    if (rejected) {
      return rejected;
    }
  }
  const path = backendPath(segments, new URL(request.url).search);
  if (!path) {
    return jsonError(404, "NOT_FOUND");
  }
  const payload = readOnly ? undefined : await readJson(request);
  const outcome = await callWithSession((accessToken) =>
    requestBackend({
      method: request.method,
      path,
      accessToken,
      payload: payload ?? undefined,
      clientIp: clientIpOf(request),
    }),
  );
  return outcome instanceof NextResponse ? outcome : relay(outcome);
}
