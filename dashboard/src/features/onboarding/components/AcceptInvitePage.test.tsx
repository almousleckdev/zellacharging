import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderWithI18n } from "@/test/renderWithI18n";
import { QueryProvider } from "@/shared/api/QueryProvider";
import { AcceptInvitePage } from "./AcceptInvitePage";

const navigation = vi.hoisted(() => ({ replace: vi.fn(), refresh: vi.fn(), search: new URLSearchParams("token=tok-1") }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace, refresh: navigation.refresh }),
  useSearchParams: () => navigation.search,
}));

type Handler = (url: string, init?: RequestInit) => Response;

function serve(handler: Handler) {
  const fetchMock = vi.fn<typeof fetch>(async (input, init) => handler(String(input), init));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

const valid = () => Response.json({ email: "new@zella.test" });
const enrollment = () =>
  Response.json({ stage: "TOTP_ENROLLMENT", challengeToken: "c-1", secret: "ABCDEFGHIJKLMNOP", provisioningUri: "otpauth://totp/Zella:x?secret=ABCDEFGHIJKLMNOP" });

function renderPage() {
  return renderWithI18n(
    <QueryProvider>
      <AcceptInvitePage />
    </QueryProvider>,
  );
}

async function setPassword(user: ReturnType<typeof userEvent.setup>, password: string, confirm = password) {
  await user.type(await screen.findByLabelText("Mot de passe"), password);
  await user.type(screen.getByLabelText("Confirmer le mot de passe"), confirm);
  await user.click(screen.getByRole("button", { name: "Créer mon mot de passe" }));
}

describe("AcceptInvitePage", () => {
  beforeEach(() => {
    navigation.replace.mockClear();
    navigation.search = new URLSearchParams("token=tok-1");
  });

  it("checks the invitation and shows the account it was sent to", async () => {
    const fetchMock = serve(valid);

    renderPage();

    expect(await screen.findByText("new@zella.test")).toBeInTheDocument();
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/auth/invitation?token=tok-1");
  });

  it("explains an expired or reused link", async () => {
    serve(() => Response.json({ code: "INVITATION_INVALID" }, { status: 404 }));

    renderPage();

    expect(await screen.findByText("Invitation invalide")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Aller à la connexion" })).toHaveAttribute("href", "/login");
  });

  it("treats a missing token as an invalid invitation without calling the server", () => {
    navigation.search = new URLSearchParams();
    const fetchMock = serve(valid);

    renderPage();

    expect(screen.getByText("Invitation invalide")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("validates the new password before sending it", async () => {
    const fetchMock = serve(valid);
    const user = userEvent.setup();
    renderPage();

    await setPassword(user, "short", "different");

    expect(screen.getAllByText("Au moins 10 caractères, avec des lettres et des chiffres.").length).toBeGreaterThan(0);
    expect(screen.getByText("Les deux mots de passe ne correspondent pas.")).toBeInTheDocument();
    expect(fetchMock.mock.calls.filter(([, init]) => init?.method === "POST")).toHaveLength(0);
  });

  it("sets the password, enrolls the authenticator and signs in", async () => {
    const fetchMock = serve((url, init) => {
      if (url === "/api/auth/invitation" && init?.method === "POST") {
        return enrollment();
      }
      if (url === "/api/auth/totp") {
        return Response.json({ ok: true });
      }
      return valid();
    });
    const user = userEvent.setup();
    renderPage();

    await setPassword(user, "Invited-Pass-42");

    expect(await screen.findByText("Sécurisez votre compte")).toBeInTheDocument();
    expect(screen.getByRole("img", { name: /QR code/ })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Retour à la connexion" })).not.toBeInTheDocument();
    await user.type(screen.getByLabelText("Code de vérification"), "123456");

    await waitFor(() => expect(navigation.replace).toHaveBeenCalledWith("/"));
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === "POST");
    expect(JSON.parse(String(post?.[1]?.body))).toEqual({ token: "tok-1", password: "Invited-Pass-42" });
  });

  it("tells the guest when the server rejects the password", async () => {
    serve((url, init) => (init?.method === "POST" ? Response.json({ code: "WEAK_PASSWORD" }, { status: 400 }) : valid()));
    const user = userEvent.setup();
    renderPage();

    await setPassword(user, "Invited-Pass-42");

    expect(await screen.findByRole("alert")).toHaveTextContent("Le nouveau mot de passe est trop faible.");
  });
});
