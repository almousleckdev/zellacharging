import { loginStepSchema } from "../schemas";
import type { Credentials, LoginStep } from "../schemas";
import type { ErrorCode } from "@/shared/i18n/types";

const ERROR_CODES: readonly ErrorCode[] = [
  "INVALID_CREDENTIALS",
  "INVALID_TOTP_CODE",
  "ACCOUNT_LOCKED",
  "TOO_MANY_REQUESTS",
  "NETWORK",
  "UNKNOWN",
  "INVITATION_INVALID",
  "WEAK_PASSWORD",
];

export class AuthApiError extends Error {
  readonly code: ErrorCode;

  constructor(code: ErrorCode) {
    super(code);
    this.code = code;
  }
}

function toErrorCode(value: unknown): ErrorCode {
  return ERROR_CODES.find((code) => code === value) ?? "UNKNOWN";
}

async function post(path: string, payload: unknown): Promise<unknown> {
  let response: Response;
  try {
    response = await fetch(path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      credentials: "same-origin",
    });
  } catch {
    throw new AuthApiError("NETWORK");
  }
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) {
    const code = typeof body === "object" && body !== null && "code" in body ? body.code : null;
    throw new AuthApiError(toErrorCode(code));
  }
  return body;
}

export async function requestLogin(credentials: Credentials): Promise<LoginStep> {
  const parsed = loginStepSchema.safeParse(await post("/api/auth/login", credentials));
  if (!parsed.success) {
    throw new AuthApiError("UNKNOWN");
  }
  return parsed.data;
}

export async function completeTotp(challengeToken: string, code: string): Promise<void> {
  await post("/api/auth/totp", { challengeToken, code });
}

export async function inspectInvitation(token: string): Promise<string> {
  let response: Response;
  try {
    response = await fetch(`/api/auth/invitation?token=${encodeURIComponent(token)}`, { credentials: "same-origin" });
  } catch {
    throw new AuthApiError("NETWORK");
  }
  const body: unknown = await response.json().catch(() => null);
  if (!response.ok || typeof body !== "object" || body === null || !("email" in body) || typeof body.email !== "string") {
    throw new AuthApiError(response.ok ? "UNKNOWN" : "INVITATION_INVALID");
  }
  return body.email;
}

export async function acceptInvitation(token: string, password: string): Promise<LoginStep> {
  const parsed = loginStepSchema.safeParse(await post("/api/auth/invitation", { token, password }));
  if (!parsed.success) {
    throw new AuthApiError("UNKNOWN");
  }
  return parsed.data;
}
