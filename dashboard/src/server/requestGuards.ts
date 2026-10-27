import "server-only";
import { NextResponse } from "next/server";

export function jsonError(status: number, code: string): NextResponse {
  return NextResponse.json({ code }, { status });
}

export function hasTrustedOrigin(request: Request): boolean {
  const origin = request.headers.get("origin");
  const host = request.headers.get("x-forwarded-host") ?? request.headers.get("host");
  if (!origin || !host) {
    return false;
  }
  try {
    return new URL(origin).host === host;
  } catch {
    return false;
  }
}

export function clientIpOf(request: Request): string | null {
  const forwarded = request.headers.get("x-forwarded-for");
  return forwarded ? (forwarded.split(",")[0]?.trim() ?? null) : null;
}

export async function readJson(request: Request): Promise<unknown> {
  if (!request.headers.get("content-type")?.includes("application/json")) {
    return null;
  }
  try {
    return await request.json();
  } catch {
    return null;
  }
}

export function guardMutation(request: Request): NextResponse | null {
  return hasTrustedOrigin(request) ? null : jsonError(403, "FORBIDDEN");
}
