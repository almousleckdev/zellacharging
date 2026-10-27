import type { z } from "zod";

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string) {
    super(code);
    this.status = status;
    this.code = code;
  }
}

function codeOf(body: unknown): string {
  return typeof body === "object" && body !== null && "code" in body && typeof body.code === "string"
    ? body.code
    : "UNKNOWN";
}

export async function adminFetch<Output>(path: string, schema: z.ZodType<Output>, init?: RequestInit): Promise<Output> {
  let response: Response;
  try {
    response = await fetch(`/api/admin/${path}`, { credentials: "same-origin", ...init });
  } catch {
    throw new ApiError(0, "NETWORK");
  }
  const body: unknown = await response.json().catch(() => null);
  if (response.status === 401) {
    window.location.replace("/login");
    throw new ApiError(401, "UNAUTHENTICATED");
  }
  if (!response.ok) {
    throw new ApiError(response.status, codeOf(body));
  }
  const parsed = schema.safeParse(body);
  if (!parsed.success) {
    throw new ApiError(502, "UNKNOWN");
  }
  return parsed.data;
}
