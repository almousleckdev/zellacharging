import "server-only";
import { postBackend } from "./backendClient";
import { tokensSchema } from "./backendSchemas";
import { clearSession, readRefreshToken, writeSession } from "./sessionCookies";
import type { SessionTokens } from "./sessionCookies";

const RETAIN_MS = 15_000;
const REJECTED_STATUSES = new Set([400, 401, 403]);

export type RefreshOutcome =
  | { kind: "renewed"; accessToken: string }
  | { kind: "rejected" }
  | { kind: "unavailable" };

type Exchange = SessionTokens | "rejected" | "unavailable";

const inflight = new Map<string, Promise<Exchange>>();

async function exchange(refreshToken: string): Promise<Exchange> {
  try {
    const result = await postBackend("/v1/admin/auth/refresh", { refreshToken }, null);
    if (REJECTED_STATUSES.has(result.status)) {
      return "rejected";
    }
    const tokens = tokensSchema.safeParse(result.body);
    return result.status === 200 && tokens.success ? tokens.data : "unavailable";
  } catch {
    return "unavailable";
  }
}

function shareExchange(refreshToken: string): Promise<Exchange> {
  const existing = inflight.get(refreshToken);
  if (existing) {
    return existing;
  }
  const started = exchange(refreshToken);
  inflight.set(refreshToken, started);
  setTimeout(() => inflight.delete(refreshToken), RETAIN_MS).unref?.();
  return started;
}

export async function refreshSession(): Promise<RefreshOutcome> {
  const refreshToken = await readRefreshToken();
  if (!refreshToken) {
    return { kind: "rejected" };
  }
  const result = await shareExchange(refreshToken);
  if (result === "unavailable") {
    return { kind: "unavailable" };
  }
  if (result === "rejected") {
    await clearSession();
    return { kind: "rejected" };
  }
  await writeSession(result);
  return { kind: "renewed", accessToken: result.accessToken };
}
