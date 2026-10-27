import "server-only";
import { cookies } from "next/headers";
import { serverEnv } from "./env";

export const ACCESS_COOKIE = "zella_at";
export const REFRESH_COOKIE = "zella_rt";

const REFRESH_MAX_AGE_SECONDS = 12 * 60 * 60;

export type SessionTokens = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
};

function options(maxAge: number) {
  return {
    httpOnly: true,
    sameSite: "strict" as const,
    secure: serverEnv().secureCookies,
    path: "/",
    maxAge,
  };
}

export async function writeSession(tokens: SessionTokens): Promise<void> {
  const store = await cookies();
  store.set(ACCESS_COOKIE, tokens.accessToken, options(tokens.expiresIn));
  store.set(REFRESH_COOKIE, tokens.refreshToken, options(REFRESH_MAX_AGE_SECONDS));
}

export async function clearSession(): Promise<void> {
  const store = await cookies();
  store.set(ACCESS_COOKIE, "", options(0));
  store.set(REFRESH_COOKIE, "", options(0));
}

export async function readAccessToken(): Promise<string | null> {
  return (await cookies()).get(ACCESS_COOKIE)?.value ?? null;
}

export async function readRefreshToken(): Promise<string | null> {
  return (await cookies()).get(REFRESH_COOKIE)?.value ?? null;
}
