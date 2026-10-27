import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import { ProfilePage } from "./ProfilePage";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/profile",
  useSearchParams: () => new URLSearchParams(),
}));

type Answers = Record<string, () => Response>;

function serve(answers: Answers) {
  const fetchMock = vi.fn(async (input: RequestInfo | URL) => {
    const answer = answers[String(input)];
    if (!answer) {
      throw new Error(`unexpected request ${String(input)}`);
    }
    return answer();
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

async function fillPasswords(user: ReturnType<typeof userEvent.setup>, current: string, next: string, confirm: string) {
  const fields: Array<[string, string]> = [
    ["Mot de passe actuel", current],
    ["Nouveau mot de passe", next],
    ["Confirmer le nouveau mot de passe", confirm],
  ];
  for (const [label, value] of fields) {
    if (value) {
      await user.type(screen.getByLabelText(label), value);
    }
  }
  await user.click(screen.getByRole("button", { name: "Mettre à jour le mot de passe" }));
}

describe("ProfilePage", () => {
  const replaceLocation = vi.fn();

  beforeEach(() => {
    replaceLocation.mockClear();
    Object.defineProperty(window, "location", {
      configurable: true,
      value: { ...window.location, replace: replaceLocation },
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows who is signed in, the role, the countries and the permissions", () => {
    renderConsole(<ProfilePage />);

    const account = screen.getByRole("heading", { name: "Compte" }).closest("section") as HTMLElement;
    expect(within(account).getAllByText("root@zella.test").length).toBeGreaterThan(0);
    expect(within(account).getAllByText("Super administrateur").length).toBeGreaterThan(0);
    const countries = screen.getByRole("heading", { name: "Pays autorisés" }).closest("section") as HTMLElement;
    expect(within(countries).getByText("Gabon")).toBeInTheDocument();
    expect(within(countries).getByText("Mali")).toBeInTheDocument();
    expect(screen.getByText("Consulter la vue d'ensemble")).toBeInTheDocument();
    expect(screen.getByText("Gérer les administrateurs")).toBeInTheDocument();
  });

  it("shows only the countries a country administrator may use", () => {
    renderConsole(<ProfilePage />, profileOf([gabon], "ADMIN"));

    const countries = screen.getByRole("heading", { name: "Pays autorisés" }).closest("section") as HTMLElement;
    expect(within(countries).getByText("Gabon")).toBeInTheDocument();
    expect(within(countries).queryByText("Mali")).not.toBeInTheDocument();
  });

  it("reports that two-factor authentication is on", () => {
    renderConsole(<ProfilePage />);

    expect(screen.getByText("Authentification à deux facteurs")).toBeInTheDocument();
    expect(screen.getByText("Activée")).toBeInTheDocument();
  });

  it("validates the new password before calling the server", async () => {
    const fetchMock = serve({});
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);

    await fillPasswords(user, "", "short", "different");

    expect(screen.getByText("Saisissez votre mot de passe actuel.")).toBeInTheDocument();
    expect(screen.getByText("Au moins 10 caractères, avec des lettres et des chiffres.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("asks for the confirmation to match", async () => {
    const fetchMock = serve({});
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);

    await fillPasswords(user, "Old-Password-1", "New-Password-22", "New-Password-23");

    expect(screen.getByText("Les deux mots de passe ne correspondent pas.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("explains a refused current password", async () => {
    serve({
      "/api/admin/me/password": () => Response.json({ code: "INVALID_CURRENT_PASSWORD" }, { status: 400 }),
    });
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);

    await fillPasswords(user, "Wrong-Password-1", "New-Password-22", "New-Password-22");

    expect(await screen.findByRole("alert")).toHaveTextContent("Le mot de passe actuel est incorrect.");
    expect(replaceLocation).not.toHaveBeenCalled();
  });

  it("changes the password, confirms it, then signs the admin out", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const fetchMock = serve({
      "/api/admin/me/password": () => new Response(null, { status: 204 }),
      "/api/auth/logout": () => new Response(null, { status: 204 }),
    });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    renderConsole(<ProfilePage />);

    await fillPasswords(user, "Old-Password-1", "New-Password-22", "New-Password-22");

    expect(await screen.findByRole("status")).toHaveTextContent("Mot de passe modifié.");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/admin/me/password",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ currentPassword: "Old-Password-1", newPassword: "New-Password-22" }),
      }),
    );
    await vi.advanceTimersByTimeAsync(3000);
    await waitFor(() => expect(replaceLocation).toHaveBeenCalledWith("/login"));
  });
});
