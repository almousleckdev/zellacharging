import "server-only";
import { serverEnv } from "./env";

const TIMEOUT_MS = 10_000;

export type BackendResult = {
  status: number;
  body: unknown;
};

export type BackendRequest = {
  method: string;
  path: string;
  accessToken?: string;
  payload?: unknown;
  clientIp?: string | null;
};

async function parseBody(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export async function requestBackend(request: BackendRequest): Promise<BackendResult> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (request.payload !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (request.accessToken) {
    headers.Authorization = `Bearer ${request.accessToken}`;
  }
  if (request.clientIp) {
    headers["X-Forwarded-For"] = request.clientIp;
  }
  const response = await fetch(new URL(request.path, serverEnv().apiUrl), {
    method: request.method,
    headers,
    body: request.payload === undefined ? undefined : JSON.stringify(request.payload),
    cache: "no-store",
    signal: AbortSignal.timeout(TIMEOUT_MS),
  });
  return { status: response.status, body: await parseBody(response) };
}

export function postBackend(path: string, payload: unknown, clientIp: string | null): Promise<BackendResult> {
  return requestBackend({ method: "POST", path, payload, clientIp });
}

export type BinaryResult = {
  status: number;
  contentType: string | null;
  bytes: ArrayBuffer;
};

export type BinaryRequest = {
  method: string;
  path: string;
  accessToken: string;
  body?: ArrayBuffer;
  contentType?: string | null;
};

export async function requestBackendBinary(request: BinaryRequest): Promise<BinaryResult> {
  const headers: Record<string, string> = { Authorization: `Bearer ${request.accessToken}` };
  if (request.contentType) {
    headers["Content-Type"] = request.contentType;
  }
  const response = await fetch(new URL(request.path, serverEnv().apiUrl), {
    method: request.method,
    headers,
    body: request.body,
    cache: "no-store",
    signal: AbortSignal.timeout(TIMEOUT_MS),
  });
  return { status: response.status, contentType: response.headers.get("content-type"), bytes: await response.arrayBuffer() };
}
