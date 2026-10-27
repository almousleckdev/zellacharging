import "server-only";
import { NextResponse } from "next/server";
import { requestBackendBinary } from "./backendClient";
import type { BinaryResult } from "./backendClient";
import { guardMutation, jsonError } from "./requestGuards";
import { callWithSession } from "./sessionCall";

const MAX_UPLOAD_BYTES = 1_100_000;

function codeOf(result: BinaryResult): string {
  try {
    const body = JSON.parse(new TextDecoder().decode(result.bytes)) as { code?: unknown };
    return typeof body.code === "string" ? body.code : "UNKNOWN";
  } catch {
    return "UNKNOWN";
  }
}

function relay(result: BinaryResult): NextResponse {
  if (result.status === 204) {
    return new NextResponse(null, { status: 204 });
  }
  if (result.status >= 400) {
    return jsonError(result.status, codeOf(result));
  }
  return new NextResponse(result.bytes, {
    status: 200,
    headers: {
      "Content-Type": result.contentType ?? "application/octet-stream",
      "Cache-Control": "private, max-age=300",
      "X-Content-Type-Options": "nosniff",
    },
  });
}

export async function proxyAvatar(request: Request, backendPath: string): Promise<NextResponse> {
  const readOnly = request.method === "GET";
  if (!readOnly) {
    const rejected = guardMutation(request);
    if (rejected) {
      return rejected;
    }
  }
  const body = request.method === "PUT" ? await request.arrayBuffer() : undefined;
  if (body && body.byteLength > MAX_UPLOAD_BYTES) {
    return jsonError(400, "INVALID_AVATAR");
  }
  const contentType = request.headers.get("content-type");
  const outcome = await callWithSession((accessToken) =>
    requestBackendBinary({ method: request.method, path: backendPath, accessToken, body, contentType }),
  );
  return outcome instanceof NextResponse ? outcome : relay(outcome);
}
