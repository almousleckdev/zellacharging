import { beforeEach, describe, expect, it, vi } from "vitest";
import { proxyAdminRequest } from "./adminProxy";

const backend = vi.hoisted(() => ({ requestBackend: vi.fn() }));
const cookies = vi.hoisted(() => ({ readAccessToken: vi.fn() }));
const refresh = vi.hoisted(() => ({ refreshSession: vi.fn() }));

vi.mock("./backendClient", () => backend);
vi.mock("./sessionCookies", () => cookies);
vi.mock("./sessionRefresh", () => refresh);

const ORIGIN = "https://console.zella.test";

function request(method: string, path: string, body?: unknown) {
  return new Request(`${ORIGIN}/api/admin/${path}`, {
    method,
    headers: { "content-type": "application/json", origin: ORIGIN, host: "console.zella.test" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}

const segmentsOf = (path: string) => path.split("?")[0].split("/");

describe("free-charging BFF routes", () => {
  beforeEach(() => {
    backend.requestBackend.mockReset();
    cookies.readAccessToken.mockReset();
    refresh.refreshSession.mockReset();
    cookies.readAccessToken.mockResolvedValue("access-1");
    backend.requestBackend.mockResolvedValue({ status: 200, body: [] });
  });

  it.each([
    ["GET", "free-charging/agreements", undefined, "/v1/admin/free-charging/agreements"],
    ["GET", "free-charging/usage?page=1&size=20", undefined, "/v1/admin/free-charging/usage?page=1&size=20"],
    ["POST", "free-charging/agreements", { customerId: "d", startsOn: "2026-11-01" }, "/v1/admin/free-charging/agreements"],
    ["PUT", "free-charging/agreements/a1", { startsOn: "2026-11-01" }, "/v1/admin/free-charging/agreements/a1"],
    ["POST", "free-charging/agreements/a1/suspend", {}, "/v1/admin/free-charging/agreements/a1/suspend"],
    ["POST", "free-charging/agreements/a1/resume", {}, "/v1/admin/free-charging/agreements/a1/resume"],
    ["POST", "free-charging/agreements/a1/revoke", {}, "/v1/admin/free-charging/agreements/a1/revoke"],
  ])("%s %s reaches the backend path with the session token", async (method, path, body, backendPath) => {
    await proxyAdminRequest(request(method, path, body), segmentsOf(path));

    expect(backend.requestBackend).toHaveBeenCalledWith(expect.objectContaining({ method, path: backendPath, accessToken: "access-1" }));
  });

  it("rejects a request without a session", async () => {
    cookies.readAccessToken.mockResolvedValue(null);
    refresh.refreshSession.mockResolvedValue({ kind: "rejected" });

    const response = await proxyAdminRequest(request("GET", "free-charging/agreements"), ["free-charging", "agreements"]);

    expect(response.status).toBe(401);
    expect(backend.requestBackend).not.toHaveBeenCalled();
  });

  it("relays the stable error code and never the server message", async () => {
    backend.requestBackend.mockResolvedValue({ status: 503, body: { code: "FREE_CHARGING_UNAVAILABLE", message: "internal detail" } });

    const response = await proxyAdminRequest(request("GET", "free-charging/agreements"), ["free-charging", "agreements"]);

    expect(response.status).toBe(503);
    expect(JSON.stringify(await response.json())).not.toContain("internal detail");
  });

  it("refuses a mutation from another origin", async () => {
    const foreign = new Request(`${ORIGIN}/api/admin/free-charging/agreements`, {
      method: "POST",
      headers: { "content-type": "application/json", origin: "https://evil.test", host: "console.zella.test" },
      body: "{}",
    });

    const response = await proxyAdminRequest(foreign, ["free-charging", "agreements"]);

    expect(response.status).toBe(403);
    expect(backend.requestBackend).not.toHaveBeenCalled();
  });
});
