import { screen, within } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { CustomerDetailPage } from "./CustomerDetailPage";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/customers/GA/c1",
  useSearchParams: () => new URLSearchParams("country=GA"),
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
  it("asks the server for this customer in this country", async () => {
    const fetchMock = serve(detail);

    renderConsole(<CustomerDetailPage />);

    await screen.findByRole("heading", { name: "Aline Obame", level: 1 });
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1");
  });

  it("shows identity, wallet and charging activity", async () => {
    serve(detail);

    renderConsole(<CustomerDetailPage />);

    const identity = (await screen.findByRole("heading", { name: "Identité" })).closest("section") as HTMLElement;
    expect(within(identity).getByText("aline@example.com")).toBeInTheDocument();
    expect(within(identity).getByText("Vérifié")).toBeInTheDocument();
    expect(within(identity).getByText("Non vérifié")).toBeInTheDocument();
    const wallet = screen.getByRole("heading", { name: "Portefeuille" }).closest("section") as HTMLElement;
    expect(wallet.textContent?.replace(/[  ]/g, " ")).toContain("5 000 FCFA");
    const activity = screen.getByRole("heading", { name: "Activité de charge" }).closest("section") as HTMLElement;
    expect(within(activity).getByText("31,5 kWh")).toBeInTheDocument();
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
