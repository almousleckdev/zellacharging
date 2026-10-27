import { NextResponse } from "next/server";
import { postBackend } from "@/server/backendClient";
import { clientIpOf, guardMutation } from "@/server/requestGuards";
import { clearSession, readRefreshToken } from "@/server/sessionCookies";

export async function POST(request: Request) {
  const rejected = guardMutation(request);
  if (rejected) {
    return rejected;
  }
  const refreshToken = await readRefreshToken();
  if (refreshToken) {
    await postBackend("/v1/admin/auth/logout", { refreshToken }, clientIpOf(request)).catch(() => null);
  }
  await clearSession();
  return new NextResponse(null, { status: 204 });
}
