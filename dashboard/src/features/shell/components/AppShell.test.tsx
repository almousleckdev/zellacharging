import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { QueryProvider } from "@/shared/api/QueryProvider";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
import { ToastProvider } from "@/shared/ui/ToastProvider";
import { AppShell } from "./AppShell";

const navigation = vi.hoisted(() => ({
  replace: vi.fn(),
  search: new URLSearchParams(),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace }),
  usePathname: () => "/",
  useSearchParams: () => navigation.search,
}));

const gabon = { code: "GA", name: "Gabon", currency: "XAF", currencyDecimals: 0 };
const mali = { code: "ML", name: "Mali", currency: "XOF", currencyDecimals: 0 };

function profile(countries = [gabon, mali], role = "SUPER_ADMIN") {
  return {
    id: "1",
    email: "root@zella.test",
    firstName: "Root",
    lastName: "Admin",
    phone: null,
    role,
    permissions: ["OVERVIEW_READ", "CUSTOMERS_READ"],
    countries,
    profileComplete: true,
    avatarVersion: null,
  };
}

type Answers = Record<string, () => Response | Promise<Response>>;

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

function renderShell() {
  return render(
    <I18nProvider>
      <QueryProvider>
        <ToastProvider>
          <AppShell>
            <p>page body</p>
          </AppShell>
        </ToastProvider>
      </QueryProvider>
    </I18nProvider>,
  );
}

describe("AppShell", () => {
  const replaceLocation = vi.fn();

  beforeEach(() => {
    navigation.replace.mockClear();
    navigation.search = new URLSearchParams();
    replaceLocation.mockClear();
    Object.defineProperty(window, "location", {
      configurable: true,
      value: { ...window.location, replace: replaceLocation },
    });
  });

  it("shows an accessible shimmer skeleton until the session is known", async () => {
    let release: (response: Response) => void = () => undefined;
    serve({
      "/api/admin/me": () => new Promise<Response>((resolve) => (release = resolve)),
    });

    renderShell();

    const loading = screen.getByRole("status");
    expect(loading).toHaveAttribute("aria-busy", "true");
    expect(within(loading).getByText("Chargement…")).toBeInTheDocument();
    expect(loading.querySelectorAll(".skeleton").length).toBeGreaterThan(10);
    expect(screen.queryByText("page body")).not.toBeInTheDocument();

    release(Response.json(profile()));

    expect(await screen.findByText("page body")).toBeInTheDocument();
    expect(screen.queryByRole("status")).not.toBeInTheDocument();
  });

  it("shows the signed-in admin and the navigation with unfinished modules disabled", async () => {
    serve({ "/api/admin/me": () => Response.json(profile()) });

    renderShell();

    expect(await screen.findByText("Root Admin")).toBeInTheDocument();
    expect(screen.getByText("Super administrateur")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Vue d'ensemble" })).toHaveAttribute("aria-current", "page");
    expect(screen.getByRole("link", { name: "Clients" })).toBeInTheDocument();
    expect(screen.getAllByText("Bientôt")).toHaveLength(7);
  });

  it("lets a multi-country admin switch the data scope through the URL", async () => {
    serve({ "/api/admin/me": () => Response.json(profile()) });
    const user = userEvent.setup();

    renderShell();
    const select = await screen.findByLabelText("Pays");
    expect(select).toHaveValue("ALL");
    expect(within(select).getAllByRole("option").map((option) => option.textContent)).toEqual([
      "Tous les pays",
      "Gabon",
      "Mali",
    ]);

    await user.selectOptions(select, "ML");

    expect(navigation.replace).toHaveBeenCalledWith("/?country=ML");
  });

  it("shows a fixed country for a single-country admin", async () => {
    serve({ "/api/admin/me": () => Response.json(profile([gabon], "ADMIN")) });

    renderShell();

    expect(await screen.findByText("Administrateur")).toBeInTheDocument();
    expect(screen.queryByLabelText("Pays")).not.toBeInTheDocument();
    expect(screen.getAllByText("Gabon").length).toBeGreaterThan(0);
  });

  it("asks for confirmation before signing out and keeps the session on cancel", async () => {
    const fetchMock = serve({ "/api/admin/me": () => Response.json(profile()) });
    const user = userEvent.setup();

    renderShell();
    await user.click(await screen.findByRole("button", { name: "Se déconnecter" }));

    const dialog = screen.getByRole("dialog", { name: "Se déconnecter ?" });
    expect(dialog).toBeInTheDocument();
    await user.click(within(dialog).getByRole("button", { name: "Annuler" }));

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(replaceLocation).not.toHaveBeenCalled();
  });

  it("signs out through the server and returns to the login page", async () => {
    const fetchMock = serve({
      "/api/admin/me": () => Response.json(profile()),
      "/api/auth/logout": () => new Response(null, { status: 204 }),
    });
    const user = userEvent.setup();

    renderShell();
    await user.click(await screen.findByRole("button", { name: "Se déconnecter" }));
    const dialog = screen.getByRole("dialog", { name: "Se déconnecter ?" });
    await user.click(within(dialog).getByRole("button", { name: "Se déconnecter" }));

    await waitFor(() => expect(replaceLocation).toHaveBeenCalledWith("/login"));
    expect(fetchMock).toHaveBeenCalledWith("/api/auth/logout", expect.objectContaining({ method: "POST" }));
  });

  it("tells the admin when sign-out fails and keeps them signed in", async () => {
    serve({
      "/api/admin/me": () => Response.json(profile()),
      "/api/auth/logout": () => Response.json({ code: "UNKNOWN" }, { status: 502 }),
    });
    const user = userEvent.setup();

    renderShell();
    await user.click(await screen.findByRole("button", { name: "Se déconnecter" }));
    const dialog = screen.getByRole("dialog", { name: "Se déconnecter ?" });
    await user.click(within(dialog).getByRole("button", { name: "Se déconnecter" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Impossible de joindre le serveur.");
    expect(replaceLocation).not.toHaveBeenCalled();
  });

  it("offers a retry when the session cannot be loaded", async () => {
    const fetchMock = serve({ "/api/admin/me": () => Response.json({ code: "FORBIDDEN" }, { status: 403 }) });
    const user = userEvent.setup();

    renderShell();

    expect(await screen.findByRole("alert")).toHaveTextContent("Impossible de charger votre session.");
    await user.click(screen.getByRole("button", { name: "Réessayer" }));
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(2));
  });

  it("sends the admin to the login page when the session has expired", async () => {
    serve({ "/api/admin/me": () => Response.json({ code: "UNAUTHENTICATED" }, { status: 401 }) });

    renderShell();

    await waitFor(() => expect(replaceLocation).toHaveBeenCalledWith("/login"));
  });

  it("keeps the selected country when moving between pages", async () => {
    navigation.search = new URLSearchParams("country=ML");
    serve({ "/api/admin/me": () => Response.json(profile()) });

    renderShell();

    expect(await screen.findByRole("link", { name: "Mon profil" })).toHaveAttribute("href", "/profile?country=ML");
    expect(screen.getByRole("link", { name: "Vue d'ensemble" })).toHaveAttribute("href", "/?country=ML");
  });

  it("leaves links untouched while no country has been chosen", async () => {
    serve({ "/api/admin/me": () => Response.json(profile()) });

    renderShell();

    expect(await screen.findByRole("link", { name: "Mon profil" })).toHaveAttribute("href", "/profile");
  });
});
