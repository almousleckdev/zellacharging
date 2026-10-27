import "server-only";
import { NextResponse } from "next/server";
import { jsonError } from "./requestGuards";
import { readAccessToken } from "./sessionCookies";
import { refreshSession } from "./sessionRefresh";

export async function callWithSession<T extends { status: number }>(
  send: (accessToken: string) => Promise<T>,
): Promise<T | NextResponse> {
  try {
    const current = await readAccessToken();
    let result = current ? await send(current) : null;
    if (!result || result.status === 401) {
      const renewed = await refreshSession();
      if (renewed.kind === "unavailable") {
        return jsonError(502, "NETWORK");
      }
      if (renewed.kind === "rejected") {
        return jsonError(401, "UNAUTHENTICATED");
      }
      result = await send(renewed.accessToken);
    }
    return result;
  } catch {
    return jsonError(502, "NETWORK");
  }
}
