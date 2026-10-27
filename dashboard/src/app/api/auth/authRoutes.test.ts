import { beforeEach, describe, expect, it, vi } from "vitest";
import { POST as login } from "./login/route";
import { POST as totp } from "./totp/route";

const backend = vi.hoisted(() => ({ postBackend: vi.fn(), getBackend: vi.fn() }));
const session = vi.hoisted(() => ({ writeSession: vi.fn(), clearSession: vi.fn(), readRefreshToken: vi.fn() }));

vi.mock("@/server/backendClient", () => backend);
vi.mock("@/server/sessionCookies", () => session);

const ORIGIN = "https://console.zella.test";

function post(path: string, body: unknown, headers: Record<string, string> = {}) {
  return new Request(`${ORIGIN}${path}`, {
    method: "POST",
    headers: { "content-type": "application/json", origin: ORIGIN, host: "console.zella.test", ...headers },
    body: JSON.stringify(body),
  });
}

const credentials = { email: "root@zella.test", password: "Str0ng-Pass!" };

describe("POST /api/auth/login", () => {
  beforeEach(() => {
    backend.postBackend.mockReset();
    session.writeSession.mockReset();
  });

  it("refuses a request that does not come from the console origin", async () => {
    const response = await login(post("/api/auth/login", credentials, { origin: "https://evil.test" }));

    expect(response.status).toBe(403);
    expect(backend.postBackend).not.toHaveBeenCalled();
  });

  it("refuses a request without an origin header", async () => {
    const request = new Request(`${ORIGIN}/api/auth/login`, {
      method: "POST",
      headers: { "content-type": "application/json", host: "console.zella.test" },
      body: JSON.stringify(credentials),
    });

    expect((await login(request)).status).toBe(403);
  });

  it("rejects a malformed body without calling the backend", async () => {
    const response = await login(post("/api/auth/login", { email: "nope", password: "" }));

    expect(response.status).toBe(400);
    expect(backend.postBackend).not.toHaveBeenCalled();
  });

  it("relays the TOTP step and the visitor's address", async () => {
    backend.postBackend.mockResolvedValue({
      status: 200,
      body: { stage: "TOTP_REQUIRED", challengeToken: "c-1", secret: null, provisioningUri: null },
    });

    const response = await login(post("/api/auth/login", credentials, { "x-forwarded-for": "203.0.113.9, 10.0.0.1" }));

    expect(await response.json()).toEqual({ stage: "TOTP_REQUIRED", challengeToken: "c-1" });
    expect(backend.postBackend).toHaveBeenCalledWith("/v1/admin/auth/login", credentials, "203.0.113.9");
  });

  it("relays only known error codes and hides everything else", async () => {
    backend.postBackend.mockResolvedValue({ status: 401, body: { code: "INVALID_CREDENTIALS", detail: "secret" } });
    const known = await login(post("/api/auth/login", credentials));
    expect(known.status).toBe(401);
    expect(await known.json()).toEqual({ code: "INVALID_CREDENTIALS" });

    backend.postBackend.mockResolvedValue({ status: 500, body: { detail: "stack trace" } });
    const unknown = await login(post("/api/auth/login", credentials));
    expect(unknown.status).toBe(502);
    expect(await unknown.json()).toEqual({ code: "UNKNOWN" });
  });

  it("reports an unreachable backend", async () => {
    backend.postBackend.mockRejectedValue(new Error("ECONNREFUSED"));

    const response = await login(post("/api/auth/login", credentials));

    expect(response.status).toBe(502);
    expect(await response.json()).toEqual({ code: "NETWORK" });
  });
});

describe("POST /api/auth/totp", () => {
  beforeEach(() => {
    backend.postBackend.mockReset();
    session.writeSession.mockReset();
  });

  const submission = { challengeToken: "c-1", code: "123456" };

  it("stores the tokens in cookies and never returns them to the browser", async () => {
    const tokens = { accessToken: "access-jwt", refreshToken: "refresh-opaque", expiresIn: 600 };
    backend.postBackend.mockResolvedValue({ status: 200, body: tokens });

    const response = await totp(post("/api/auth/totp", submission));
    const text = await response.text();

    expect(session.writeSession).toHaveBeenCalledWith(tokens);
    expect(JSON.parse(text)).toEqual({ ok: true });
    expect(text).not.toContain("access-jwt");
    expect(text).not.toContain("refresh-opaque");
  });

  it("does not open a session when the code is wrong", async () => {
    backend.postBackend.mockResolvedValue({ status: 401, body: { code: "INVALID_TOTP_CODE" } });

    const response = await totp(post("/api/auth/totp", submission));

    expect(response.status).toBe(401);
    expect(session.writeSession).not.toHaveBeenCalled();
  });

  it("surfaces a locked account", async () => {
    backend.postBackend.mockResolvedValue({ status: 429, body: { code: "ACCOUNT_LOCKED" } });

    const response = await totp(post("/api/auth/totp", submission));

    expect(response.status).toBe(429);
    expect(await response.json()).toEqual({ code: "ACCOUNT_LOCKED" });
  });

  it("rejects a code that is not six digits", async () => {
    const response = await totp(post("/api/auth/totp", { challengeToken: "c-1", code: "12ab56" }));

    expect(response.status).toBe(400);
    expect(backend.postBackend).not.toHaveBeenCalled();
  });

  it("refuses to open a session from a malformed backend answer", async () => {
    backend.postBackend.mockResolvedValue({ status: 200, body: { accessToken: "only-one" } });

    const response = await totp(post("/api/auth/totp", submission));

    expect(response.status).toBe(502);
    expect(session.writeSession).not.toHaveBeenCalled();
  });
});
