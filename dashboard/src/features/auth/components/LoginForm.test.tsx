import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderWithI18n } from "@/test/renderWithI18n";
import { LoginForm } from "./LoginForm";

const router = vi.hoisted(() => ({ replace: vi.fn(), refresh: vi.fn() }));

vi.mock("next/navigation", () => ({ useRouter: () => router }));

type Reply = { status: number; body: unknown } | "offline";

function replyWith(...replies: Reply[]) {
  const fetchMock = vi.fn();
  for (const reply of replies) {
    if (reply === "offline") {
      fetchMock.mockRejectedValueOnce(new TypeError("offline"));
    } else {
      fetchMock.mockResolvedValueOnce(Response.json(reply.body, { status: reply.status }));
    }
  }
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

async function signIn(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText("Adresse e-mail"), "root@zella.test");
  await user.type(screen.getByLabelText("Mot de passe"), "Str0ng-Pass!");
  await user.click(screen.getByRole("button", { name: "Se connecter" }));
}

describe("LoginForm", () => {
  beforeEach(() => {
    router.replace.mockClear();
    router.refresh.mockClear();
  });

  it("asks for both fields in French before calling the server", async () => {
    const fetchMock = replyWith();
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await user.click(screen.getByRole("button", { name: "Se connecter" }));

    expect(screen.getByText("Saisissez votre adresse e-mail.")).toBeInTheDocument();
    expect(screen.getByText("Saisissez votre mot de passe.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("rejects a malformed email address", async () => {
    replyWith();
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await user.type(screen.getByLabelText("Adresse e-mail"), "not-an-email");
    await user.type(screen.getByLabelText("Mot de passe"), "x");
    await user.click(screen.getByRole("button", { name: "Se connecter" }));

    expect(screen.getByText("Saisissez une adresse e-mail valide.")).toBeInTheDocument();
  });

  it("toggles password visibility", async () => {
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    const field = screen.getByLabelText("Mot de passe");
    expect(field).toHaveAttribute("type", "password");
    await user.click(screen.getByRole("button", { name: "Afficher le mot de passe" }));
    expect(field).toHaveAttribute("type", "text");
  });

  it("shows a generic message for wrong credentials", async () => {
    replyWith({ status: 401, body: { code: "INVALID_CREDENTIALS" } });
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);

    expect(await screen.findByRole("alert")).toHaveTextContent("Adresse e-mail ou mot de passe incorrect.");
  });

  it("explains when the server cannot be reached", async () => {
    replyWith("offline");
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);

    expect(await screen.findByRole("alert")).toHaveTextContent("Impossible de joindre le serveur.");
  });

  it("asks for the authenticator code after the password and signs in with it", async () => {
    const fetchMock = replyWith(
      { status: 200, body: { stage: "TOTP_REQUIRED", challengeToken: "challenge-1" } },
      { status: 200, body: { ok: true } },
    );
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);
    expect(await screen.findByText("Vérification en deux étapes")).toBeInTheDocument();
    await user.type(screen.getByLabelText("Code de vérification"), "123456");

    await waitFor(() => expect(router.replace).toHaveBeenCalledWith("/"));
    expect(fetchMock).toHaveBeenLastCalledWith(
      "/api/auth/totp",
      expect.objectContaining({ body: JSON.stringify({ challengeToken: "challenge-1", code: "123456" }) }),
    );
  });

  it("keeps non-digits out of the code field", async () => {
    replyWith({ status: 200, body: { stage: "TOTP_REQUIRED", challengeToken: "c" } });
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);
    const code = await screen.findByLabelText("Code de vérification");
    await user.type(code, "12ab3");

    expect(code).toHaveValue("123");
  });

  it("clears the code and explains a rejected code", async () => {
    replyWith(
      { status: 200, body: { stage: "TOTP_REQUIRED", challengeToken: "c" } },
      { status: 401, body: { code: "INVALID_TOTP_CODE" } },
    );
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);
    const code = await screen.findByLabelText("Code de vérification");
    await user.type(code, "000000");

    expect(await screen.findByRole("alert")).toHaveTextContent("Code de vérification invalide ou expiré.");
    expect(code).toHaveValue("");
    expect(router.replace).not.toHaveBeenCalled();
  });

  it("shows the QR code and manual key on first sign-in", async () => {
    replyWith({
      status: 200,
      body: {
        stage: "TOTP_ENROLLMENT",
        challengeToken: "c",
        secret: "ABCDEFGHIJKLMNOP",
        provisioningUri: "otpauth://totp/Zella:root?secret=ABCDEFGHIJKLMNOP",
      },
    });
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);

    expect(await screen.findByText("Sécurisez votre compte")).toBeInTheDocument();
    expect(screen.getByRole("img", { name: /QR code/ })).toBeInTheDocument();
    expect(screen.getByText("ABCD EFGH IJKL MNOP")).toBeInTheDocument();
  });

  it("returns to the password step", async () => {
    replyWith({ status: 200, body: { stage: "TOTP_REQUIRED", challengeToken: "c" } });
    const user = userEvent.setup();
    renderWithI18n(<LoginForm />);

    await signIn(user);
    await user.click(await screen.findByRole("button", { name: "Retour à la connexion" }));

    expect(screen.getByLabelText("Adresse e-mail")).toBeInTheDocument();
  });
});
