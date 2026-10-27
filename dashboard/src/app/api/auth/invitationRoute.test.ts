import { beforeEach, describe, expect, it, vi } from "vitest";
import { GET, POST } from "./invitation/route";

const backend = vi.hoisted(() => ({ requestBackend: vi.fn(), postBackend: vi.fn() }));

vi.mock("@/server/backendClient", () => backend);

const ORIGIN = "https://console.zella.test";

function post(body: unknown, headers: Record<string, string> = {}) {
  return new Request(`${ORIGIN}/api/auth/invitation`, {
    method: "POST",
    headers: { "content-type": "application/json", origin: ORIGIN, host: "console.zella.test", ...headers },
    body: JSON.stringify(body),
  });
}

describe("invitation routes", () => {
  beforeEach(() => {
    backend.requestBackend.mockReset();
    backend.postBackend.mockReset();
  });

  it("rejects a request without a token before calling the backend", async () => {
    const response = await GET(new Request(`${ORIGIN}/api/auth/invitation`));

    expect(response.status).toBe(400);
    expect(backend.requestBackend).not.toHaveBeenCalled();
  });

  it("returns only the invited email for a valid token", async () => {
    backend.requestBackend.mockResolvedValue({ status: 200, body: { email: "new@zella.test", extra: "x" } });

    const response = await GET(new Request(`${ORIGIN}/api/auth/invitation?token=tok%2Fa`));

    expect(await response.json()).toEqual({ email: "new@zella.test" });
    expect(backend.requestBackend).toHaveBeenCalledWith(
      expect.objectContaining({ path: "/v1/admin/auth/invitation?token=tok%2Fa" }),
    );
  });

  it("relays an invalid invitation and hides other failures", async () => {
    backend.requestBackend.mockResolvedValue({ status: 404, body: { code: "INVITATION_INVALID" } });
    const invalid = await GET(new Request(`${ORIGIN}/api/auth/invitation?token=old`));
    expect(invalid.status).toBe(404);
    expect(await invalid.json()).toEqual({ code: "INVITATION_INVALID" });

    backend.requestBackend.mockResolvedValue({ status: 500, body: { detail: "stack" } });
    const broken = await GET(new Request(`${ORIGIN}/api/auth/invitation?token=old`));
    expect(broken.status).toBe(502);
  });

  it("accepts the invitation and returns the next authentication step without any token", async () => {
    backend.postBackend.mockResolvedValue({
      status: 200,
      body: { stage: "TOTP_ENROLLMENT", challengeToken: "c-1", secret: "ABCDEFGHIJKLMNOP", provisioningUri: "otpauth://x" },
    });

    const response = await POST(post({ token: "tok", password: "Invited-Pass-42" }));

    expect(await response.json()).toMatchObject({ stage: "TOTP_ENROLLMENT", challengeToken: "c-1" });
    expect(backend.postBackend).toHaveBeenCalledWith("/v1/admin/auth/invitation", { token: "tok", password: "Invited-Pass-42" }, null);
  });

  it("refuses to accept from another origin or with an incomplete body", async () => {
    expect((await POST(post({ token: "t", password: "p" }, { origin: "https://evil.test" }))).status).toBe(403);
    expect((await POST(post({ token: "", password: "" }))).status).toBe(400);
    expect(backend.postBackend).not.toHaveBeenCalled();
  });

  it("relays a weak-password refusal", async () => {
    backend.postBackend.mockResolvedValue({ status: 400, body: { code: "WEAK_PASSWORD" } });

    const response = await POST(post({ token: "tok", password: "weak" }));

    expect(response.status).toBe(400);
    expect(await response.json()).toEqual({ code: "WEAK_PASSWORD" });
  });
});
