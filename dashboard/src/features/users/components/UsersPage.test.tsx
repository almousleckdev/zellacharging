import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { UsersPage } from "./UsersPage";

const navigation = vi.hoisted(() => ({ replace: vi.fn(), search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace }),
  usePathname: () => "/users",
  useSearchParams: () => navigation.search,
}));

function user(overrides: Record<string, unknown> = {}) {
  return {
    id: "u1",
    email: "aline@zella.test",
    firstName: "Aline",
    lastName: "Obame",
    phone: null,
    role: "SUPPORT",
    status: "ACTIVE",
    permissions: ["OVERVIEW_READ", "CUSTOMERS_READ", "SESSIONS_READ"],
    countries: ["GA"],
    lastLoginAt: "2026-10-20T08:30:00Z",
    avatarVersion: null,
    ...overrides,
  };
}

type Handler = (url: string, init?: RequestInit) => Response;

function serve(handler: Handler) {
  const fetchMock = vi.fn<typeof fetch>(async (input, init) => handler(String(input), init));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

const page = (items: unknown[]) => Response.json({ total: items.length, page: 0, size: 20, items });
const manager = () => profileOf(undefined, "SUPER_ADMIN");

describe("UsersPage", () => {
  beforeEach(() => {
    navigation.replace.mockClear();
    navigation.search = new URLSearchParams();
  });

  it("lists users with role, countries, status and last sign-in", async () => {
    serve(() => page([user(), user({ id: "u2", email: "new@zella.test", firstName: null, lastName: null, status: "INVITED", lastLoginAt: null, role: "FINANCE", countries: ["GA", "ML"] })]));

    renderConsole(<UsersPage />, manager());

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Aline Obame")).toBeInTheDocument();
    expect(within(table).getByText("Invitation envoyée")).toBeInTheDocument();
    expect(within(table).getByText("Profil non complété")).toBeInTheDocument();
    expect(within(table).getByText("Gabon, Mali")).toBeInTheDocument();
    expect(within(table).getByText("Jamais")).toBeInTheDocument();
  });

  it("refuses to show the module to someone who may not manage users", () => {
    serve(() => page([]));

    renderConsole(<UsersPage />, { ...profileOf(), permissions: ["OVERVIEW_READ"] });

    expect(screen.getByText("Vous n'avez pas la permission d'effectuer cette action.")).toBeInTheDocument();
  });

  it("offers only the actions that fit the user's status", async () => {
    serve(() => page([user({ id: "a", status: "ACTIVE" }), user({ id: "b", email: "b@zella.test", status: "INVITED" }), user({ id: "c", email: "c@zella.test", status: "DISABLED" })]));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    const buttons = await screen.findAllByRole("button", { name: /Actions pour/ });
    await userEvents.click(buttons[0]);
    expect(screen.getByRole("menuitem", { name: "Désactiver" })).toBeInTheDocument();
    expect(screen.queryByRole("menuitem", { name: "Renvoyer l'invitation" })).not.toBeInTheDocument();
    await userEvents.keyboard("{Escape}");
    await userEvents.click(buttons[1]);
    expect(screen.getByRole("menuitem", { name: "Renvoyer l'invitation" })).toBeInTheDocument();
    expect(screen.getByRole("menuitem", { name: "Supprimer l'invitation" })).toBeInTheDocument();
    await userEvents.keyboard("{Escape}");
    await userEvents.click(buttons[2]);
    expect(screen.getByRole("menuitem", { name: "Réactiver" })).toBeInTheDocument();
  });

  it("never offers actions on the super administrator", async () => {
    serve(() => page([user({ role: "SUPER_ADMIN", countries: [] })]));

    renderConsole(<UsersPage />, manager());

    await screen.findByRole("table");
    expect(screen.queryByRole("button", { name: /Actions pour/ })).not.toBeInTheDocument();
  });

  it("requires an email and at least one country before inviting", async () => {
    const fetchMock = serve(() => page([]));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: "Inviter un utilisateur" }));
    const dialog = screen.getByRole("dialog");
    await userEvents.click(within(dialog).getByRole("button", { name: "Envoyer l'invitation" }));

    expect(within(dialog).getByText("Saisissez votre adresse e-mail.")).toBeInTheDocument();
    expect(within(dialog).getByText("Sélectionnez au moins un pays.")).toBeInTheDocument();
    expect(fetchMock.mock.calls.filter(([, init]) => init?.method === "POST")).toHaveLength(0);
  });

  it("invites a user with the chosen role, countries and permissions", async () => {
    const fetchMock = serve((url, init) =>
      init?.method === "POST" ? Response.json({ admin: user({ id: "n1", email: "new@zella.test", status: "INVITED" }), emailSent: true }, { status: 201 }) : page([]),
    );
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: "Inviter un utilisateur" }));
    const dialog = screen.getByRole("dialog");
    await userEvents.type(within(dialog).getByLabelText("Adresse e-mail"), "new@zella.test");
    await userEvents.selectOptions(within(dialog).getByLabelText("Rôle"), "FINANCE");
    await userEvents.click(within(dialog).getByLabelText("Mali"));
    await userEvents.click(within(dialog).getByLabelText("Consulter le journal d'audit"));
    await userEvents.click(within(dialog).getByRole("button", { name: "Envoyer l'invitation" }));

    await waitFor(() => {
      const post = fetchMock.mock.calls.find(([, init]) => init?.method === "POST");
      expect(JSON.parse(String(post?.[1]?.body))).toEqual({
        email: "new@zella.test",
        role: "FINANCE",
        countries: ["ML"],
        permissions: ["OVERVIEW_READ", "CUSTOMERS_READ", "PAYMENTS_READ", "AUDIT_READ"],
      });
    });
    expect(await screen.findByText("Invitation envoyée à new@zella.test.")).toBeInTheDocument();
  });

  it("pre-fills the permissions from the role and resets them when the role changes", async () => {
    serve(() => page([]));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: "Inviter un utilisateur" }));
    const dialog = screen.getByRole("dialog");
    expect(within(dialog).getByLabelText("Consulter les paiements")).toBeChecked();
    await userEvents.selectOptions(within(dialog).getByLabelText("Rôle"), "OPERATIONS");

    expect(within(dialog).getByLabelText("Consulter les paiements")).not.toBeChecked();
    expect(within(dialog).getByLabelText("Consulter les bornes")).toBeChecked();
  });

  it("shows the server's reason when the invitation is refused", async () => {
    serve((url, init) => (init?.method === "POST" ? Response.json({ code: "EMAIL_ALREADY_USED" }, { status: 409 }) : page([])));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: "Inviter un utilisateur" }));
    const dialog = screen.getByRole("dialog");
    await userEvents.type(within(dialog).getByLabelText("Adresse e-mail"), "dup@zella.test");
    await userEvents.click(within(dialog).getByLabelText("Gabon"));
    await userEvents.click(within(dialog).getByRole("button", { name: "Envoyer l'invitation" }));

    expect(await within(dialog).findByRole("alert")).toHaveTextContent("Cette adresse e-mail est déjà utilisée.");
  });

  it("warns when the invitation email could not be sent", async () => {
    serve((url, init) => (init?.method === "POST" ? Response.json({ admin: user(), emailSent: false }, { status: 201 }) : page([])));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: "Inviter un utilisateur" }));
    const dialog = screen.getByRole("dialog");
    await userEvents.type(within(dialog).getByLabelText("Adresse e-mail"), "x@zella.test");
    await userEvents.click(within(dialog).getByLabelText("Gabon"));
    await userEvents.click(within(dialog).getByRole("button", { name: "Envoyer l'invitation" }));

    expect(await screen.findByText(/l'e-mail n'a pas pu être envoyé/)).toBeInTheDocument();
  });

  it("edits the access of an existing user", async () => {
    const fetchMock = serve((url, init) => (init?.method === "PUT" ? Response.json(user()) : page([user()])));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: /Actions pour/ }));
    await userEvents.click(screen.getByRole("menuitem", { name: "Modifier l'accès" }));
    const dialog = screen.getByRole("dialog");
    expect(within(dialog).getByLabelText("Gabon")).toBeChecked();
    await userEvents.click(within(dialog).getByLabelText("Mali"));
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => {
      const put = fetchMock.mock.calls.find(([, init]) => init?.method === "PUT");
      expect(String(put?.[0])).toBe("/api/admin/users/u1");
      expect(JSON.parse(String(put?.[1]?.body)).countries.sort()).toEqual(["GA", "ML"]);
    });
  });

  it("asks for confirmation before disabling and only then calls the server", async () => {
    const fetchMock = serve((url, init) => (init?.method === "POST" ? Response.json(user({ status: "DISABLED" })) : page([user()])));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: /Actions pour/ }));
    await userEvents.click(screen.getByRole("menuitem", { name: "Désactiver" }));
    const dialog = screen.getByRole("dialog", { name: "Désactiver cet utilisateur ?" });
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === "POST")).toBe(false);
    await userEvents.click(within(dialog).getByRole("button", { name: "Désactiver" }));

    await waitFor(() => expect(fetchMock.mock.calls.some(([input]) => String(input) === "/api/admin/users/u1/disable")).toBe(true));
    expect(await screen.findByText("Utilisateur désactivé.")).toBeInTheDocument();
  });

  it("puts the search and status filters in the URL", async () => {
    serve(() => page([]));
    const userEvents = userEvent.setup();
    renderConsole(<UsersPage />, manager());

    await userEvents.selectOptions(await screen.findByLabelText("Statut"), "INVITED");

    expect(navigation.replace).toHaveBeenCalledWith("/users?status=INVITED");
  });
});
