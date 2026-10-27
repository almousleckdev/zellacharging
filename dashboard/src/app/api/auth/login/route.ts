import { NextResponse } from "next/server";
import { credentialsSchema, loginStepSchema } from "@/features/auth/schemas";
import { proxyAuthStep } from "@/server/proxyAuthStep";

export function POST(request: Request) {
  return proxyAuthStep(request, {
    path: "/v1/admin/auth/login",
    input: credentialsSchema,
    output: loginStepSchema,
    onSuccess: async (step) => NextResponse.json(step),
  });
}
