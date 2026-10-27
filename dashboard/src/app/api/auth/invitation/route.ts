import { NextResponse } from "next/server";
import { z } from "zod";
import { loginStepSchema } from "@/features/auth/schemas";
import { requestBackend } from "@/server/backendClient";
import { clientIpOf, jsonError } from "@/server/requestGuards";
import { proxyAuthStep, relayFailure } from "@/server/proxyAuthStep";

const acceptSchema = z.object({
  token: z.string().min(1).max(200),
  password: z.string().min(1).max(128),
});

const infoSchema = z.object({ email: z.string() });

export async function GET(request: Request) {
  const token = new URL(request.url).searchParams.get("token");
  if (!token || token.length > 200) {
    return jsonError(400, "INVITATION_INVALID");
  }
  try {
    const result = await requestBackend({
      method: "GET",
      path: `/v1/admin/auth/invitation?token=${encodeURIComponent(token)}`,
      clientIp: clientIpOf(request),
    });
    const info = infoSchema.safeParse(result.body);
    return result.status === 200 && info.success ? NextResponse.json(info.data) : relayFailure(result);
  } catch {
    return jsonError(502, "NETWORK");
  }
}

export function POST(request: Request) {
  return proxyAuthStep(request, {
    path: "/v1/admin/auth/invitation",
    input: acceptSchema,
    output: loginStepSchema,
    onSuccess: async (step) => NextResponse.json(step),
  });
}
