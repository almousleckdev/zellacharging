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
const DRIVERS = [
  { driverId: "d-jean", customerId: "c-jean", fullName: "MBA Jean", phone: "+24177000001", email: "jean@example.com", fleetNumber: "V015", plate: "PL-0150", status: "ACTIVE", linked: true, inProgram: false, agreementId: null, agreementStatus: null },
  { driverId: "d-paul", customerId: "c-paul", fullName: "NDONG Paul", phone: "+24166000002", email: null, fleetNumber: "V030", plate: "PL-0300", status: "ACTIVE", linked: true, inProgram: false, agreementId: null, agreementStatus: null },
  { driverId: "d-awa", customerId: "c-awa", fullName: "OBIANG Awa", phone: null, email: null, fleetNumber: null, plate: null, status: "ACTIVE", linked: true, inProgram: true, agreementId: "ag-awa", agreementStatus: "ACTIVE" },
  { driverId: "d-luc", customerId: null, fullName: "ONDO Luc", phone: "+24162000004", email: null, fleetNumber: "V041", plate: "PL-0410", status: "ACTIVE", linked: false, inProgram: false, agreementId: null, agreementStatus: null },
  { driverId: "d-zoe", customerId: "c-zoe", fullName: "ZOGO Zoé", phone: null, email: null, fleetNumber: null, plate: null, status: "ARCHIVED", linked: true, inProgram: false, agreementId: null, agreementStatus: null },
];
const driverPage = (items = DRIVERS, total = items.length) => ({ items, page: 0, size: 25, total });

function withDrivers(handler: Handler): Handler {
  return (url, init) => (url.includes("/api/admin/free-charging/drivers") ? Response.json(driverPage()) : handler(url, init));
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

  it("shows the car and the phone of each driver and counts who is in the program", async () => {
    serve(() => Response.json([
      agreement({ fleetNumber: "V015", plate: "PL-0150", phone: "+24177001122" }),
      agreement({ id: "a2", driverName: "Paul Ndong", eligibility: "UNPAID_DAYS", unpaidDays: 2, fleetNumber: "V030", plate: "PL-0300", phone: "+24166002233" }),
      agreement({ id: "a3", driverName: "Awa Obiang", status: "REVOKED", eligibility: "NO_AGREEMENT" }),
    ]));

    renderConsole(<FreeChargingPage />, reader());

    const table = await screen.findByRole("table");
    expect(within(table).getByText("V015 · PL-0150")).toBeInTheDocument();
    expect(within(table).getByText("+24177001122")).toBeInTheDocument();
    const stats = screen.getByText("Dans le programme").closest("article") as HTMLElement;
    expect(within(stats).getByText("2")).toBeInTheDocument();
    expect(within(screen.getByText("Éligibles maintenant").closest("article") as HTMLElement).getByText("1")).toBeInTheDocument();
    expect(within(screen.getByText("À régulariser").closest("article") as HTMLElement).getByText("1")).toBeInTheDocument();
  });

  it("filters the list by name, phone, e-mail or car number", async () => {
    serve(() => Response.json([
      agreement({ fleetNumber: "V015", plate: "PL-0150", phone: "+24177001122", email: "jean@example.com" }),
      agreement({ id: "a2", driverName: "Paul Ndong", fleetNumber: "V030", plate: "PL-0300", phone: "+24166002233", email: "paul@example.com" }),
    ]));
    const userEvents = userEvent.setup();
    renderConsole(<FreeChargingPage />, reader());
    await screen.findByRole("table");
    const search = screen.getByRole("searchbox");

    await userEvents.type(search, "V030");
    await waitFor(() => expect(screen.queryByText("Jean Mba")).not.toBeInTheDocument());
    expect(screen.getByText("Paul Ndong")).toBeInTheDocument();

    await userEvents.clear(search);
    await userEvents.type(search, "077 00 11 22");
    await waitFor(() => expect(screen.queryByText("Paul Ndong")).not.toBeInTheDocument());
    expect(screen.getByText("Jean Mba")).toBeInTheDocument();

    await userEvents.clear(search);
    await userEvents.type(search, "zzzz");
    expect(await screen.findByText("Aucun chauffeur ne correspond à cette recherche.")).toBeInTheDocument();
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

  async function pick(userEvents: ReturnType<typeof userEvent.setup>, dialog: HTMLElement, name: string) {
    await userEvents.click(await within(dialog).findByRole("checkbox", { name: new RegExp(name) }));
  }

  const created = (fetchMock: ReturnType<typeof serve>) => posts(fetchMock).filter(([url]) => String(url) === "/api/admin/free-charging/agreements").map(([, init]) => JSON.parse(String(init?.body)));

  const retire = (fetchMock: ReturnType<typeof serve>) => posts(fetchMock).filter(([url]) => String(url).endsWith("/revoke")).map(([url]) => String(url));
  const reply = (url: string, init?: RequestInit) => (init?.method === "POST" ? Response.json(agreement(), { status: 201 }) : Response.json([]));

  it("lists every driver in a table with their car, status and place in the program", async () => {
    serve(withDrivers(() => Response.json([])));
    const { dialog } = await openAddDialog();

    const table = await within(dialog).findByRole("table");
    expect(within(table).getAllByRole("row")).toHaveLength(DRIVERS.length + 1);
    expect(within(table).getByText("V015 · PL-0150")).toBeInTheDocument();
    expect(within(table).getByText("Démissionnaire")).toBeInTheDocument();
    expect(within(table).getByText("Dans le programme")).toBeInTheDocument();
    expect(within(table).getByText("Compte Zella non lié")).toBeInTheDocument();
  });

  it("shows a driver in the program as checked and one who is not as unchecked", async () => {
    serve(withDrivers(() => Response.json([])));
    const { dialog } = await openAddDialog();

    expect(await within(dialog).findByRole("checkbox", { name: /OBIANG Awa/ })).toBeChecked();
    expect(within(dialog).getByRole("checkbox", { name: /MBA Jean/ })).not.toBeChecked();
    expect(within(dialog).getByRole("checkbox", { name: /ONDO Luc/ })).toBeDisabled();
    expect(within(dialog).getByRole("checkbox", { name: /ZOGO/ })).toBeDisabled();
  });

  it("asks for at least one change", async () => {
    const fetchMock = serve(withDrivers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();
    await within(dialog).findByRole("table");

    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    expect(within(dialog).getByText("Cochez ou décochez au moins un chauffeur.")).toBeInTheDocument();
    expect(posts(fetchMock)).toHaveLength(0);
  });

  it("asks for the start date once a driver is ticked, and refuses an end before it", async () => {
    const fetchMock = serve(withDrivers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();
    await pick(userEvents, dialog, "MBA Jean");

    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));
    expect(within(dialog).getByText("Choisissez la date de début.")).toBeInTheDocument();
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-10");
    await userEvents.type(within(dialog).getByLabelText("Fin (facultatif)"), "2026-11-02");
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    expect(within(dialog).getByText("La fin ne peut pas précéder le début.")).toBeInTheDocument();
    expect(posts(fetchMock)).toHaveLength(0);
  });

  it("explains what an empty end date means", async () => {
    serve(withDrivers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();
    await pick(userEvents, dialog, "MBA Jean");

    expect(within(dialog).getByText("Sans date de fin, l'accord reste actif jusqu'à ce que vous le suspendiez ou le retiriez.")).toBeInTheDocument();
  });

  it("creates the agreement for a ticked driver with the entered terms", async () => {
    const fetchMock = serve(withDrivers(reply));
    const { userEvents, dialog } = await openAddDialog();

    await pick(userEvents, dialog, "MBA Jean");
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");
    await userEvents.type(within(dialog).getByLabelText("Conditions (facultatif)"), "Loyer à jour");
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    await waitFor(() => expect(created(fetchMock)).toEqual([{ customerId: "c-jean", startsOn: "2026-11-01", conditions: "Loyer à jour" }]));
    expect(await screen.findByText("1 ajouté(s), 0 retiré(s).")).toBeInTheDocument();
  });

  it("removes a driver from the program by unticking them, without asking for dates", async () => {
    const fetchMock = serve(withDrivers(reply));
    const { userEvents, dialog } = await openAddDialog();

    await pick(userEvents, dialog, "OBIANG Awa");
    expect(within(dialog).queryByLabelText("Début")).not.toBeInTheDocument();
    expect(within(dialog).getByText("Sera retiré")).toBeInTheDocument();
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    await waitFor(() => expect(retire(fetchMock)).toEqual(["/api/admin/free-charging/agreements/ag-awa/revoke"]));
    expect(created(fetchMock)).toHaveLength(0);
  });

  it("adds and removes in one save", async () => {
    const fetchMock = serve(withDrivers(reply));
    const { userEvents, dialog } = await openAddDialog();

    await pick(userEvents, dialog, "MBA Jean");
    await pick(userEvents, dialog, "NDONG Paul");
    await pick(userEvents, dialog, "OBIANG Awa");
    expect(within(dialog).getByText("2 à ajouter · 1 à retirer")).toBeInTheDocument();
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    await waitFor(() => expect(created(fetchMock).map((body) => body.customerId)).toEqual(["c-jean", "c-paul"]));
    expect(retire(fetchMock)).toHaveLength(1);
  });

  it("ticks every driver of the page who can be added in one click, never the blocked ones, and cancels them", async () => {
    serve(withDrivers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();
    await within(dialog).findByRole("table");

    await userEvents.click(within(dialog).getByRole("checkbox", { name: "Sélectionner les chauffeurs de la page" }));
    expect(within(dialog).getByRole("checkbox", { name: /MBA Jean/ })).toBeChecked();
    expect(within(dialog).getByRole("checkbox", { name: /NDONG Paul/ })).toBeChecked();
    expect(within(dialog).getByRole("checkbox", { name: /ONDO Luc/ })).not.toBeChecked();
    await userEvents.click(within(dialog).getByRole("button", { name: "Annuler les changements" }));

    expect(within(dialog).getByRole("checkbox", { name: /MBA Jean/ })).not.toBeChecked();
    expect(within(dialog).getByRole("checkbox", { name: /OBIANG Awa/ })).toBeChecked();
  });

  it("searches the drivers on the typed text, a car number included", async () => {
    const fetchMock = serve(withDrivers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();
    await within(dialog).findByRole("checkbox", { name: /MBA Jean/ });

    await userEvents.type(within(dialog).getByRole("searchbox"), "V015");

    await waitFor(() => expect(fetchMock.mock.calls.map(([url]) => String(url))).toContain("/api/admin/free-charging/drivers?membership=ALL&page=0&size=25&q=V015"));
  });

  it("filters the table on who is in the program", async () => {
    const fetchMock = serve(withDrivers(() => Response.json([])));
    const { userEvents, dialog } = await openAddDialog();
    await within(dialog).findByRole("table");

    await userEvents.click(within(dialog).getByRole("button", { name: "Dans le programme" }));

    await waitFor(() => expect(fetchMock.mock.calls.map(([url]) => String(url))).toContain("/api/admin/free-charging/drivers?membership=IN_PROGRAM&page=0&size=25"));
  });

  it("keeps the ticks when the page changes", async () => {
    const fetchMock = serve((url, init) => {
      if (!url.includes("/free-charging/drivers")) return init?.method === "POST" ? Response.json(agreement(), { status: 201 }) : Response.json([]);
      return url.includes("page=1") ? Response.json({ items: [DRIVERS[1]], page: 1, size: 25, total: 30 }) : Response.json({ items: [DRIVERS[0]], page: 0, size: 25, total: 30 });
    });
    const { userEvents, dialog } = await openAddDialog();
    await pick(userEvents, dialog, "MBA Jean");

    await userEvents.click(within(dialog).getByRole("button", { name: /Suivant/ }));
    await pick(userEvents, dialog, "NDONG Paul");
    expect(within(dialog).getByText("2 à ajouter · 0 à retirer")).toBeInTheDocument();
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    await waitFor(() => expect(created(fetchMock).map((body) => body.customerId)).toEqual(["c-jean", "c-paul"]));
  });

  it("lists the changes that failed and retries only those", async () => {
    let paulFails = true;
    const fetchMock = serve(withDrivers((url, init) => {
      if (init?.method !== "POST") return Response.json([]);
      const customer = JSON.parse(String(init.body)).customerId;
      return customer === "c-paul" && paulFails ? Response.json({ code: "FREE_AGREEMENT_EXISTS" }, { status: 422 }) : Response.json(agreement(), { status: 201 });
    }));
    const { userEvents, dialog } = await openAddDialog();
    await pick(userEvents, dialog, "MBA Jean");
    await pick(userEvents, dialog, "NDONG Paul");
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");

    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    expect(await within(dialog).findByText("1 chauffeur ajouté.")).toBeInTheDocument();
    expect(within(dialog).getByText("NDONG Paul : Ce chauffeur a déjà un accord de gratuité en cours.")).toBeInTheDocument();
    paulFails = false;
    await userEvents.click(within(dialog).getByRole("button", { name: "Réessayer" }));

    await waitFor(() => expect(created(fetchMock).map((body) => body.customerId)).toEqual(["c-jean", "c-paul", "c-paul"]));
  });

  it.each([
    ["FREE_AGREEMENT_EXISTS", 422, "Ce chauffeur a déjà un accord de gratuité en cours."],
    ["FREE_CHARGING_UNAVAILABLE", 503, "Le service NDZela est momentanément injoignable. Réessayez dans un instant."],
    ["FREE_AGREEMENT_NOT_LINKED", 422, "Ce client n'est pas un chauffeur NDZela lié : vérifiez le lien sur sa fiche client."],
  ])("shows a readable message for %s and never the raw server text", async (code, status, message) => {
    serve(withDrivers((url, init) => (init?.method === "POST" ? Response.json({ code, message: "raw server text" }, { status }) : Response.json([]))));
    const { userEvents, dialog } = await openAddDialog();

    await pick(userEvents, dialog, "MBA Jean");
    await userEvents.type(within(dialog).getByLabelText("Début"), "2026-11-01");
    await userEvents.click(within(dialog).getByRole("button", { name: "Enregistrer les changements" }));

    expect(await within(dialog).findByText(new RegExp(message.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")))).toBeInTheDocument();
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
