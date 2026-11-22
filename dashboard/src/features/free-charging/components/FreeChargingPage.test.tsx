import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { FreeChargingPage } from "./FreeChargingPage";

const navigation = vi.hoisted(() => ({ replace: vi.fn(), search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace, push: vi.fn() }),
  usePathname: () => "/free-charging",
  useSearchParams: () => navigation.search,
}));

const DRIVER_ID = "3f2c1b7e-5a41-4c1d-9a47-0d2f6c8b9e10";
const CUSTOMER_ID = "9b1d4c52-7f0e-4a35-8c26-5e3a1d7b0f64";

function customerPage() {
  const item = { id: CUSTOMER_ID, country: "GA", firstName: "Jean", lastName: "Mba", email: "jean@example.com", phone: "+24177000001", status: "ACTIVE", createdAt: "2026-01-01T00:00:00Z", ndzelaDriver: true };
  return { total: 1, page: 0, size: 8, items: [item] };
}

function withCustomers(handler: Handler): Handler {
  return (url, init) => (url.includes("/api/admin/customers") ? Response.json(customerPage()) : handler(url, init));
}

function agreement(overrides: Record<string, unknown> = {}) {
  return {
    id: "a1",
    driverId: DRIVER_ID,
    driverName: "Jean Mba",
    startsOn: "2026-11-01",
    endsOn: null,
    status: "ACTIVE",
    conditions: "Loyer à jour",
    eligibility: "ELIGIBLE",
    unpaidDays: 0,
    lastFreeUse: null,
    ...overrides,
  };
}

type Handler = (url: string, init?: RequestInit) => Response;

function serve(handler: Handler) {
  const fetchMock = vi.fn<typeof fetch>(async (input, init) => handler(String(input), init));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

const withPermissions = (...permissions: string[]) => ({ ...profileOf(), permissions });
const manager = () => withPermissions("FREE_CHARGING_READ", "FREE_CHARGING_MANAGE");
const reader = () => withPermissions("FREE_CHARGING_READ");
const posts = (fetchMock: ReturnType<typeof serve>) => fetchMock.mock.calls.filter(([, init]) => init?.method === "POST");

describe("FreeChargingPage drivers", () => {
  beforeEach(() => {
    navigation.replace.mockClear();
    navigation.search = new URLSearchParams();
  });

  it("shows status, eligibility label and unpaid days for each driver", async () => {
    serve(() => Response.json([agreement({ status: "SUSPENDED", eligibility: "OUTSIDE_WINDOW", unpaidDays: 2 }), agreement({ id: "a2", driverName: "Paul Ndong", eligibility: "UNPAID_DAYS", unpaidDays: 1 })]));

    renderConsole(<FreeChargingPage />, reader());

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Jean Mba")).toBeInTheDocument();
    expect(within(table).getByText("Suspendu")).toBeInTheDocument();
    expect(within(table).getByText("Hors créneau")).toBeInTheDocument();
    expect(within(table).getByText("2")).toBeInTheDocument();
    expect(within(table).getAllByText("01/11/2026")).toHaveLength(2);
  });

  it("hides every management control without the manage permission", async () => {
    serve(() => Response.json([agreement()]));

    renderConsole(<FreeChargingPage />, reader());

    await screen.findByRole("table");
    expect(screen.queryByRole("button", { name: "Ajouter un chauffeur" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /Actions pour/ })).not.toBeInTheDocument();
  });

  it("shows the management controls with the manage permission", async () => {
    serve(() => Response.json([agreement()]));
    const userEvents = userEvent.setup();

    renderConsole(<FreeChargingPage />, manager());

    expect(await screen.findByRole("button", { name: "Ajouter un chauffeur" })).toBeInTheDocument();
    await userEvents.click(await screen.findByRole("button", { name: /Actions pour/ }));
    expect(screen.getByRole("menuitem", { name: "Modifier" })).toBeInTheDocument();
    expect(screen.getByRole("menuitem", { name: "Suspendre" })).toBeInTheDocument();
    expect(screen.getByRole("menuitem", { name: "Retirer" })).toBeInTheDocument();
  });

  it("refuses the page without the read permission", () => {
    serve(() => Response.json([]));

    renderConsole(<FreeChargingPage />, withPermissions("OVERVIEW_READ"));

    expect(screen.getByText("Vous n'avez pas la permission d'effectuer cette action.")).toBeInTheDocument();
  });

  it("suspends and resumes through the BFF", async () => {
    const fetchMock = serve((url, init) => (init?.method === "POST" ? Response.json(agreement()) : Response.json([agreement(), agreement({ id: "a2", driverName: "Paul Ndong", status: "SUSPENDED" })])));
    const userEvents = userEvent.setup();
    renderConsole(<FreeChargingPage />, manager());

    const menus = await screen.findAllByRole("button", { name: /Actions pour/ });
    await userEvents.click(menus[0]);
    await userEvents.click(screen.getByRole("menuitem", { name: "Suspendre" }));
    await waitFor(() => expect(posts(fetchMock).map(([url]) => String(url))).toContain("/api/admin/free-charging/agreements/a1/suspend"));
    await userEvents.click(menus[1]);
    await userEvents.click(screen.getByRole("menuitem", { name: "Reprendre" }));
    await waitFor(() => expect(posts(fetchMock).map(([url]) => String(url))).toContain("/api/admin/free-charging/agreements/a2/resume"));
  });

  it("asks before revoking, says the history is kept, then calls revoke", async () => {
    const fetchMock = serve((url, init) => (init?.method === "POST" ? Response.json(agreement({ status: "REVOKED" })) : Response.json([agreement()])));
    const userEvents = userEvent.setup();
    renderConsole(<FreeChargingPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: /Actions pour/ }));
    await userEvents.click(screen.getByRole("menuitem", { name: "Retirer" }));
    const dialog = screen.getByRole("dialog", { name: "Retirer ce chauffeur ?" });
    expect(within(dialog).getByText(/L'historique de l'accord est conservé/)).toBeInTheDocument();
    expect(posts(fetchMock)).toHaveLength(0);
    await userEvents.click(within(dialog).getByRole("button", { name: "Retirer" }));

    await waitFor(() => expect(posts(fetchMock).map(([url]) => String(url))).toContain("/api/admin/free-charging/agreements/a1/revoke"));
  });

  it("keeps the revoke dialog open and explains when revoking fails", async () => {
    serve((url, init) => (init?.method === "POST" ? Response.json({ code: "FREE_CHARGING_UNAVAILABLE" }, { status: 503 }) : Response.json([agreement()])));
    const userEvents = userEvent.setup();
    renderConsole(<FreeChargingPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: /Actions pour/ }));
    await userEvents.click(screen.getByRole("menuitem", { name: "Retirer" }));
    const dialog = screen.getByRole("dialog", { name: "Retirer ce chauffeur ?" });
    await userEvents.click(within(dialog).getByRole("button", { name: "Retirer" }));

    expect(await within(dialog).findByRole("alert")).toHaveTextContent("Le service NDZela est momentanément injoignable. Réessayez dans un instant.");
    expect(screen.getByRole("dialog", { name: "Retirer ce chauffeur ?" })).toBeInTheDocument();
  });

  it("shows an empty state when there is no agreement", async () => {
    serve(() => Response.json([]));

    renderConsole(<FreeChargingPage />, manager());

    expect(await screen.findByText("Aucun chauffeur dans le programme")).toBeInTheDocument();
  });
});

describe("FreeChargingPage add and edit", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams();
  });

  async function openAddDialog() {
    const userEvents = userEvent.setup();
    renderConsole(<FreeChargingPage />, manager());
    await userEvents.click(await screen.findByRole("button", { name: "Ajouter un chauffeur" }));
    return { userEvents, dialog: screen.getByRole("dialog") };
  }

  async function pickJean(userEvents: ReturnType<typeof userEvent.setup>, dialog: HTMLElement) {
    await userEvents.click(await within(dialog).findByRole("option", { name: /Jean Mba/ }));
  }

  it("requires the customer and the start date", async () => {
    const fetchMock = serve(withCustomers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();

    await userEvents.click(within(dialog).getByRole("button", { name: "Ajouter" }));

    expect(within(dialog).getByText("Choisissez le client à ajouter.")).toBeInTheDocument();
    expect(within(dialog).getByText("Choisissez la date de début.")).toBeInTheDocument();
    expect(posts(fetchMock)).toHaveLength(0);
  });

  it("refuses an end date before the start date", async () => {
    const fetchMock = serve(withCustomers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();

    await pickJean(userEvents, dialog);
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-10");
    await userEvents.type(within(dialog).getByLabelText("Fin (facultatif)"), "2026-11-02");
    await userEvents.click(within(dialog).getByRole("button", { name: "Ajouter" }));

    expect(within(dialog).getByText("La fin ne peut pas précéder le début.")).toBeInTheDocument();
    expect(posts(fetchMock)).toHaveLength(0);
  });

  it("creates the agreement with the chosen customer and the entered terms", async () => {
    const fetchMock = serve(withCustomers((url, init) => (init?.method === "POST" ? Response.json(agreement(), { status: 201 }) : Response.json([]))));
    const { userEvents, dialog } = await openAddDialog();

    await pickJean(userEvents, dialog);
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");
    await userEvents.type(within(dialog).getByLabelText("Conditions (facultatif)"), "Loyer à jour");
    await userEvents.click(within(dialog).getByRole("button", { name: "Ajouter" }));

    await waitFor(() => {
      const post = posts(fetchMock)[0];
      expect(String(post[0])).toBe("/api/admin/free-charging/agreements");
      expect(JSON.parse(String(post[1]?.body))).toEqual({ customerId: CUSTOMER_ID, startsOn: "2026-11-01", conditions: "Loyer à jour" });
    });
    expect(await screen.findByText("Chauffeur ajouté.")).toBeInTheDocument();
  });

  it.each([
    ["FREE_AGREEMENT_EXISTS", 422, "Ce chauffeur a déjà un accord de gratuité en cours."],
    ["FREE_CHARGING_UNAVAILABLE", 503, "Le service NDZela est momentanément injoignable. Réessayez dans un instant."],
    ["FREE_AGREEMENT_NOT_LINKED", 422, "Ce client n'est pas un chauffeur NDZela lié : vérifiez le lien sur sa fiche client."],
  ])("shows a readable message for %s and keeps the form open", async (code, status, message) => {
    serve(withCustomers((url, init) => (init?.method === "POST" ? Response.json({ code, message: "raw server text" }, { status }) : Response.json([]))));
    const { userEvents, dialog } = await openAddDialog();

    await pickJean(userEvents, dialog);
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");
    await userEvents.click(within(dialog).getByRole("button", { name: "Ajouter" }));

    expect(await within(dialog).findByRole("alert")).toHaveTextContent(message);
    expect(screen.queryByText("raw server text")).not.toBeInTheDocument();
  });

  it("edits the dates and conditions of an agreement", async () => {
    const fetchMock = serve((url, init) => (init?.method === "PUT" ? Response.json(agreement()) : Response.json([agreement()])));
    const userEvents = userEvent.setup();
    renderConsole(<FreeChargingPage />, manager());

    await userEvents.click(await screen.findByRole("button", { name: /Actions pour/ }));
    await userEvents.click(screen.getByRole("menuitem", { name: "Modifier" }));
    const dialog = screen.getByRole("dialog");
    expect(within(dialog).queryByRole("listbox")).not.toBeInTheDocument();
    await userEvents.type(within(dialog).getByLabelText("Fin (facultatif)"), "2026-12-31");
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => {
      const put = fetchMock.mock.calls.find(([, init]) => init?.method === "PUT");
      expect(String(put?.[0])).toBe("/api/admin/free-charging/agreements/a1");
      expect(JSON.parse(String(put?.[1]?.body))).toEqual({ startsOn: "2026-11-01", endsOn: "2026-12-31", conditions: "Loyer à jour" });
    });
  });
});
