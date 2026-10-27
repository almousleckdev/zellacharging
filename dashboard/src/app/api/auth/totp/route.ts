import { NextResponse } from "next/server";
import { totpSubmissionSchema } from "@/features/auth/schemas";
import { tokensSchema } from "@/server/backendSchemas";
import { proxyAuthStep } from "@/server/proxyAuthStep";
import { writeSession } from "@/server/sessionCookies";

export function POST(request: Request) {
  return proxyAuthStep(request, {
    path: "/v1/admin/auth/totp",
    input: totpSubmissionSchema,
    output: tokensSchema,
    onSuccess: async (tokens) => {
      await writeSession(tokens);
      return NextResponse.json({ ok: true });
    },
  });
}
