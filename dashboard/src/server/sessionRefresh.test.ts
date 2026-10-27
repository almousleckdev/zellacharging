import { beforeEach, describe, expect, it, vi } from "vitest";
import { refreshSession } from "./sessionRefresh";

const backend = vi.hoisted(() => ({ postBackend: vi.fn() }));
const cookies = vi.hoisted(() => ({
  readRefreshToken: vi.fn(),
  writeSession: vi.fn(),
  clearSession: vi.fn(),
}));

vi.mock("./backendClient", () => backend);
vi.mock("./sessionCookies", () => cookies);

const tokens = { accessToken: "new-access", refreshToken: "new-refresh", expiresIn: 600 };

describe("session refresh", () => {
  beforeEach(() => {
    backend.postBackend.mockReset();
    cookies.writeSession.mockReset();
    cookies.clearSession.mockReset();
    cookies.readRefreshToken.mockReset();
  });

  it("exchanges the refresh token only once for parallel requests", async () => {
    cookies.readRefreshToken.mockResolvedValue("same-refresh");
    backend.postBackend.mockResolvedValue({ status: 200, body: tokens });

    const results = await Promise.all([refreshSession(), refreshSession(), refreshSession()]);

    expect(results).toEqual(Array(3).fill({ kind: "renewed", accessToken: "new-access" }));
    expect(backend.postBackend).toHaveBeenCalledTimes(1);
    expect(cookies.writeSession).toHaveBeenCalledTimes(3);
  });

  it("clears the session when the backend rejects the refresh token", async () => {
    cookies.readRefreshToken.mockResolvedValue("revoked-refresh");
    backend.postBackend.mockResolvedValue({ status: 401, body: { code: "INVALID_REFRESH_TOKEN" } });

    expect(await refreshSession()).toEqual({ kind: "rejected" });
    expect(cookies.clearSession).toHaveBeenCalled();
    expect(cookies.writeSession).not.toHaveBeenCalled();
  });

  it("does nothing without a refresh cookie", async () => {
    cookies.readRefreshToken.mockResolvedValue(null);

    expect(await refreshSession()).toEqual({ kind: "rejected" });
    expect(backend.postBackend).not.toHaveBeenCalled();
  });

  it("keeps the session when the backend cannot be reached", async () => {
    cookies.readRefreshToken.mockResolvedValue("outage-refresh");
    backend.postBackend.mockRejectedValue(new Error("ECONNREFUSED"));

    expect(await refreshSession()).toEqual({ kind: "unavailable" });
    expect(cookies.clearSession).not.toHaveBeenCalled();
  });

  it("keeps the session when the backend answers with a server error", async () => {
    cookies.readRefreshToken.mockResolvedValue("error-refresh");
    backend.postBackend.mockResolvedValue({ status: 503, body: null });

    expect(await refreshSession()).toEqual({ kind: "unavailable" });
    expect(cookies.clearSession).not.toHaveBeenCalled();
  });

  it("keeps the session when the backend answer is malformed", async () => {
    cookies.readRefreshToken.mockResolvedValue("other-refresh");
    backend.postBackend.mockResolvedValue({ status: 200, body: { accessToken: "only" } });

    expect(await refreshSession()).toEqual({ kind: "unavailable" });
    expect(cookies.clearSession).not.toHaveBeenCalled();
  });
});
