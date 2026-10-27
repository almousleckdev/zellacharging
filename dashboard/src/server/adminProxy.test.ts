import { beforeEach, describe, expect, it, vi } from "vitest";
import { proxyAdminRequest } from "./adminProxy";

const backend = vi.hoisted(() => ({ requestBackend: vi.fn() }));
const cookies = vi.hoisted(() => ({ readAccessToken: vi.fn() }));
const refresh = vi.hoisted(() => ({ refreshSession: vi.fn() }));

vi.mock("./backendClient", () => backend);
vi.mock("./sessionCookies", () => cookies);
vi.mock("./sessionRefresh", () => refresh);

const ORIGIN = "https://console.zella.test";

function get(path: string) {
  return new Request(`${ORIGIN}/api/admin/${path}`);
}

function mutation(path: string, headers: Record<string, string> = {}) {
  return new Request(`${ORIGIN}/api/admin/${path}`, {
    method: "POST",
    headers: { "content-type": "application/json", origin: ORIGIN, host: "console.zella.test", ...headers },
    body: JSON.stringify({ amount: 5 }),
  });
}

describe("admin proxy", () => {
  beforeEach(() => {
    backend.requestBackend.mockReset();
    cookies.readAccessToken.mockReset();
    refresh.refreshSession.mockReset();
    cookies.readAccessToken.mockResolvedValue("access-1");
  });

  it("forwards a read with the access token and returns the payload", async () => {
    backend.requestBackend.mockResolvedValue({ status: 200, body: { email: "root@zella.test" } });

    const response = await proxyAdminRequest(get("me?country=GA"), ["me"]);

    expect(await response.json()).toEqual({ email: "root@zella.test" });
    expect(backend.requestBackend).toHaveBeenCalledWith(
      expect.objectContaining({ method: "GET", path: "/v1/admin/me?country=GA", accessToken: "access-1" }),
    );
  });

  it("never reaches the backend auth endpoints or odd paths", async () => {
    for (const segments of [["auth", "login"], ["..", "secret"], ["me", "x%2f"], []]) {
      const response = await proxyAdminRequest(get("x"), segments);
      expect(response.status).toBe(404);
    }
    expect(backend.requestBackend).not.toHaveBeenCalled();
  });

  it("refuses a mutation that does not come from the console origin", async () => {
    const response = await proxyAdminRequest(mutation("wallets/1/adjust", { origin: "https://evil.test" }), [
      "wallets",
      "1",
      "adjust",
    ]);

    expect(response.status).toBe(403);
    expect(backend.requestBackend).not.toHaveBeenCalled();
  });

  it("renews an expired session once and retries with the new token", async () => {
    backend.requestBackend
      .mockResolvedValueOnce({ status: 401, body: { code: "UNAUTHENTICATED" } })
      .mockResolvedValueOnce({ status: 200, body: { ok: true } });
    refresh.refreshSession.mockResolvedValue({ kind: "renewed", accessToken: "access-2" });

    const response = await proxyAdminRequest(get("me"), ["me"]);

    expect(response.status).toBe(200);
    expect(backend.requestBackend).toHaveBeenLastCalledWith(expect.objectContaining({ accessToken: "access-2" }));
    expect(refresh.refreshSession).toHaveBeenCalledTimes(1);
  });

  it("renews the session when the access cookie has already expired", async () => {
    cookies.readAccessToken.mockResolvedValue(null);
    refresh.refreshSession.mockResolvedValue({ kind: "renewed", accessToken: "access-3" });
    backend.requestBackend.mockResolvedValue({ status: 200, body: { ok: true } });

    const response = await proxyAdminRequest(get("me"), ["me"]);

    expect(response.status).toBe(200);
    expect(backend.requestBackend).toHaveBeenCalledTimes(1);
    expect(backend.requestBackend).toHaveBeenCalledWith(expect.objectContaining({ accessToken: "access-3" }));
  });

  it("answers 401 when the session cannot be renewed", async () => {
    backend.requestBackend.mockResolvedValue({ status: 401, body: null });
    refresh.refreshSession.mockResolvedValue({ kind: "rejected" });

    const response = await proxyAdminRequest(get("me"), ["me"]);

    expect(response.status).toBe(401);
    expect(await response.json()).toEqual({ code: "UNAUTHENTICATED" });
  });

  it("reports a network problem instead of a sign-out when the renewal cannot be attempted", async () => {
    backend.requestBackend.mockResolvedValue({ status: 401, body: null });
    refresh.refreshSession.mockResolvedValue({ kind: "unavailable" });

    const response = await proxyAdminRequest(get("me"), ["me"]);

    expect(response.status).toBe(502);
    expect(await response.json()).toEqual({ code: "NETWORK" });
  });

  it("does not loop when the retry is still unauthorized", async () => {
    backend.requestBackend.mockResolvedValue({ status: 401, body: { code: "UNAUTHENTICATED" } });
    refresh.refreshSession.mockResolvedValue({ kind: "renewed", accessToken: "access-2" });

    const response = await proxyAdminRequest(get("me"), ["me"]);

    expect(response.status).toBe(401);
    expect(backend.requestBackend).toHaveBeenCalledTimes(2);
  });

  it("relays only the error code and hides backend details", async () => {
    backend.requestBackend.mockResolvedValue({
      status: 403,
      body: { code: "COUNTRY_FORBIDDEN", detail: "internal trace", instance: "/v1/admin/x" },
    });

    const response = await proxyAdminRequest(get("overview?country=ML"), ["overview"]);

    expect(response.status).toBe(403);
    expect(await response.json()).toEqual({ code: "COUNTRY_FORBIDDEN" });
  });

  it("reports an unreachable backend", async () => {
    backend.requestBackend.mockRejectedValue(new Error("ECONNREFUSED"));

    const response = await proxyAdminRequest(get("me"), ["me"]);

    expect(response.status).toBe(502);
    expect(await response.json()).toEqual({ code: "NETWORK" });
  });
});
