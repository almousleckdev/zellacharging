import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { CustomerDetailPage } from "./CustomerDetailPage";

const navigation = vi.hoisted(() => ({ replace: vi.fn(), search: new URLSearchParams("country=GA") }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace, push: vi.fn() }),
  usePathname: () => "/customers/GA/c1",
  useSearchParams: () => navigation.search,
  useParams: () => ({ country: "GA", id: "c1" }),
}));

const detail = {
  id: "c1",
  country: "GA",
  firstName: "Aline",
  lastName: "Obame",
  email: "aline@example.com",
  phone: "+24177123456",
  status: "ACTIVE",
  createdAt: "2026-10-01T10:00:00Z",
  emailVerified: true,
  phoneVerified: false,
  wallet: {
    currency: "XAF",
    decimals: 0,
    balanceMinor: 5000,
    reservedMinor: 500,
    topupsSucceededMinor: 8000,
    pendingTopups: 1,
    withdrawalsPaidMinor: 0,
  },
  sessions: { completed: 3, live: 0, energyKwh: 31.5, spentMinor: 4725, lastSessionAt: "2026-10-20T08:30:00Z" },
};

function serve(body: unknown, status = 200) {
  const fetchMock = vi.fn<typeof fetch>(async () => Response.json(body, { status }));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("CustomerDetailPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams("country=GA");
  });

  it("asks the server for this customer in this country", async () => {
    const fetchMock = serve(detail);

    renderConsole(<CustomerDetailPage />);

    await screen.findByRole("heading", { name: "Aline Obame", level: 1 });
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1");
  });

  it("opens on an overview with the key figures, the identity and the wallet", async () => {
    serve(detail);

    renderConsole(<CustomerDetailPage />);

    const identity = (await screen.findByRole("heading", { name: "Identité" })).closest("section") as HTMLElement;
    expect(within(identity).getByText("aline@example.com")).toBeInTheDocument();
    expect(within(identity).getByText("Vérifié")).toBeInTheDocument();
    const wallet = screen.getByRole("heading", { name: "Portefeuille" }).closest("section") as HTMLElement;
    expect(wallet.textContent?.replace(/[\u00a0\u202f]/g, " ")).toContain("5 000 FCFA");
    expect(screen.getByText("31,5 kWh")).toBeInTheDocument();
    expect(screen.getByText("Total dépensé").nextElementSibling?.textContent?.replace(/[\u00a0\u202f]/g, " ")).toContain("4 725 FCFA");
  });

  it("offers the history as tabs and loads the wallet movements of this customer only when opened", async () => {
    const fetchMock = vi.fn<typeof fetch>(async (input) =>
      String(input).includes("/ledger")
        ? Response.json({ total: 1, page: 0, size: 20, currency: "XAF", decimals: 0, items: [{ id: "l1", type: "TOPUP", amountMinor: 5000, sessionId: null, topupId: "t1", createdAt: "2026-10-20T08:30:00Z" }] })
        : Response.json(detail),
    );
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();

    renderConsole(<CustomerDetailPage />, { ...profileOf(), permissions: ["OVERVIEW_READ", "SESSIONS_READ", "PAYMENTS_READ", "AUDIT_READ"] });

    for (const name of ["Aperçu", "Recharges", "Paiements", "Portefeuille", "Activité"]) {
      expect(await screen.findByRole("tab", { name })).toBeInTheDocument();
    }
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes("/ledger"))).toBe(false);
    navigation.search = new URLSearchParams("country=GA&tab=wallet");
    await user.click(screen.getByRole("tab", { name: "Portefeuille" }));
    expect(navigation.replace).toHaveBeenCalledWith(expect.stringContaining("tab=wallet"));
  });

  it("handles a customer without a wallet", async () => {
    serve({ ...detail, wallet: null });

    renderConsole(<CustomerDetailPage />);

    expect(await screen.findByText("Aucun portefeuille pour ce client.")).toBeInTheDocument();
  });

  it("says so when the customer is not in the selected country", async () => {
    serve({ code: "CUSTOMER_NOT_FOUND" }, 404);

    renderConsole(<CustomerDetailPage />);

    expect(await screen.findByText("Ce client est introuvable dans le pays sélectionné.")).toBeInTheDocument();
  });

  it("links back to the list keeping the country", async () => {
    serve(detail);

    renderConsole(<CustomerDetailPage />);

    expect(await screen.findByRole("link", { name: "Retour aux clients" })).toHaveAttribute("href", "/customers?country=GA");
  });
});
