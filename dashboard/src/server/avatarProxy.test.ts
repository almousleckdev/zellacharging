import { beforeEach, describe, expect, it, vi } from "vitest";
import { GET as usersAvatar } from "@/app/api/admin/users/[id]/avatar/route";
import { proxyAvatar } from "./avatarProxy";

const backend = vi.hoisted(() => ({ requestBackendBinary: vi.fn() }));
const cookies = vi.hoisted(() => ({ readAccessToken: vi.fn() }));
const refresh = vi.hoisted(() => ({ refreshSession: vi.fn() }));

vi.mock("./backendClient", () => backend);
vi.mock("./sessionCookies", () => cookies);
vi.mock("./sessionRefresh", () => refresh);

const ORIGIN = "https://console.zella.test";
const PNG = new Uint8Array([0x89, 0x50, 0x4e, 0x47]).buffer;

function mutation(method: string, body?: BodyInit, headers: Record<string, string> = {}) {
  return new Request(`${ORIGIN}/api/admin/me/avatar`, {
    method,
    headers: { origin: ORIGIN, host: "console.zella.test", "content-type": "image/png", ...headers },
    body,
  });
}

describe("avatar proxy", () => {
  beforeEach(() => {
    backend.requestBackendBinary.mockReset();
    cookies.readAccessToken.mockReset();
    refresh.refreshSession.mockReset();
    cookies.readAccessToken.mockResolvedValue("access-1");
  });

  it("serves the picture with the safe headers", async () => {
    backend.requestBackendBinary.mockResolvedValue({ status: 200, contentType: "image/png", bytes: PNG });

    const response = await proxyAvatar(new Request(`${ORIGIN}/api/admin/me/avatar`), "/v1/admin/me/avatar");

    expect(response.status).toBe(200);
    expect(response.headers.get("content-type")).toBe("image/png");
    expect(response.headers.get("x-content-type-options")).toBe("nosniff");
    expect(response.headers.get("cache-control")).toContain("private");
    expect(new Uint8Array(await response.arrayBuffer())).toEqual(new Uint8Array(PNG));
  });

  it("forwards an upload with its content type and the access token", async () => {
    backend.requestBackendBinary.mockResolvedValue({ status: 204, contentType: null, bytes: new ArrayBuffer(0) });

    const response = await proxyAvatar(mutation("PUT", PNG), "/v1/admin/me/avatar");

    expect(response.status).toBe(204);
    expect(backend.requestBackendBinary).toHaveBeenCalledWith(
      expect.objectContaining({ method: "PUT", accessToken: "access-1", contentType: "image/png" }),
    );
  });

  it("rejects an oversized upload before it reaches the backend", async () => {
    const response = await proxyAvatar(mutation("PUT", new Uint8Array(1_200_000)), "/v1/admin/me/avatar");

    expect(response.status).toBe(400);
    expect(await response.json()).toEqual({ code: "INVALID_AVATAR" });
    expect(backend.requestBackendBinary).not.toHaveBeenCalled();
  });

  it("refuses changes that do not come from the console origin", async () => {
    const response = await proxyAvatar(mutation("DELETE", undefined, { origin: "https://evil.test" }), "/v1/admin/me/avatar");

    expect(response.status).toBe(403);
    expect(backend.requestBackendBinary).not.toHaveBeenCalled();
  });

  it("renews an expired session and retries", async () => {
    backend.requestBackendBinary
      .mockResolvedValueOnce({ status: 401, contentType: null, bytes: new ArrayBuffer(0) })
      .mockResolvedValueOnce({ status: 200, contentType: "image/png", bytes: PNG });
    refresh.refreshSession.mockResolvedValue({ kind: "renewed", accessToken: "access-2" });

    const response = await proxyAvatar(new Request(`${ORIGIN}/api/admin/me/avatar`), "/v1/admin/me/avatar");

    expect(response.status).toBe(200);
    expect(backend.requestBackendBinary).toHaveBeenLastCalledWith(expect.objectContaining({ accessToken: "access-2" }));
  });

  it("relays only the error code from the backend", async () => {
    const body = new TextEncoder().encode(JSON.stringify({ code: "INVALID_AVATAR", detail: "internal" })).buffer;
    backend.requestBackendBinary.mockResolvedValue({ status: 400, contentType: "application/json", bytes: body });

    const response = await proxyAvatar(mutation("PUT", PNG), "/v1/admin/me/avatar");

    expect(response.status).toBe(400);
    expect(await response.json()).toEqual({ code: "INVALID_AVATAR" });
  });

  it("only proxies another user's picture for a well-formed id", async () => {
    const bad = await usersAvatar(new Request(`${ORIGIN}/x`), { params: Promise.resolve({ id: "../../etc" }) });
    expect(bad.status).toBe(404);
    expect(backend.requestBackendBinary).not.toHaveBeenCalled();

    backend.requestBackendBinary.mockResolvedValue({ status: 200, contentType: "image/png", bytes: PNG });
    const good = await usersAvatar(new Request(`${ORIGIN}/x`), { params: Promise.resolve({ id: "6f1c0d1e-8a3b-4f6e-9c1d-2a7b5e4d3c21" }) });
    expect(good.status).toBe(200);
    expect(backend.requestBackendBinary).toHaveBeenCalledWith(
      expect.objectContaining({ path: "/v1/admin/users/6f1c0d1e-8a3b-4f6e-9c1d-2a7b5e4d3c21/avatar" }),
    );
  });
});
