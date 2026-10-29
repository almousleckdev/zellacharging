import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import { OverviewPage } from "./OverviewPage";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/",
  useSearchParams: () => navigation.search,
}));

function figures(code: string, name: string, currency: string, scale: number) {
  return {
    code,
    name,
    currency,
    currencyDecimals: 0,
    customers: { total: 10 * scale, active: 8 * scale, newLast30Days: 2 * scale },
    sessions: { live: scale, completed: 5 * scale, startedLast24Hours: 3 * scale, energyKwh: 40.5 * scale, revenueMinor: 6000 * scale },
    wallets: {
      count: 9 * scale,
      balanceMinor: 5000 * scale,
      reservedMinor: 500 * scale,
      topupsSucceededMinor: 9000 * scale,
      pendingTopups: scale,
      withdrawalsPaidMinor: 2000 * scale,
      pendingWithdrawals: 0,
      capturedMinor: 138 * scale,
    },
  };
}

function point(scale: number) {
  return {
    day: new Date().toISOString().slice(0, 10),
    topupsMinor: 9000 * scale,
    topupCount: 4,
    withdrawalsMinor: 2000 * scale,
    capturedMinor: 138 * scale,
    sessions: 6,
    completed: 5,
    failed: 1,
    energyKwh: 40.5,
    revenueMinor: 6000,
    serviceFeeMinor: 20 * scale,
    vatMinor: 0,
  };
}

function serve(countries: ReturnType<typeof figures>[], since?: string) {
  const fetchMock = vi.fn(async (input: RequestInfo | URL) => {
    const url = String(input);
    if (url.includes("/overview")) {
      return Response.json({ scope: "ALL", since, countries });
    }
    if (url.includes("/series")) {
      return Response.json({
        countries: countries.map((item, index) => ({
          code: item.code,
          name: item.name,
          currency: item.currency,
          decimals: 0,
          points: [point(index + 1)],
        })),
      });
    }
    if (url.includes("/incidents")) {
      return Response.json({ total: 4, open: 4, openHigh: 2, page: 0, size: 1, items: [] });
    }
    return Response.json({ total: 0, page: 0, size: 6, items: [] });
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

function text(node: HTMLElement): string {
  return node.textContent?.replace(/[\u00a0\u202f]/g, " ") ?? "";
}

describe("OverviewPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams();
  });

  it("shows shimmer cards while the figures load", () => {
    vi.stubGlobal("fetch", vi.fn(() => new Promise<Response>(() => undefined)));

    renderConsole(<OverviewPage />);

    const loading = screen.getByRole("status");
    expect(loading).toHaveAttribute("aria-busy", "true");
    expect(loading.querySelectorAll(".skeleton").length).toBeGreaterThan(20);
  });

  it("asks the server for the selected country only", async () => {
    navigation.search = new URLSearchParams("country=GA");
    const fetchMock = serve([figures("GA", "Gabon", "XAF", 1)]);

    renderConsole(<OverviewPage />);

    expect(await screen.findByText("Ventes (recharges facturées)")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith("/api/admin/overview?country=GA", expect.anything());
    expect(fetchMock.mock.calls.some(([url]) => String(url).startsWith("/api/admin/series?country=GA"))).toBe(true);
  });

  it("shows the period key figures with their money and the money panel", async () => {
    navigation.search = new URLSearchParams("country=GA");
    serve([figures("GA", "Gabon", "XAF", 1)], "2026-10-28T00:00:00Z");

    renderConsole(<OverviewPage />, profileOf([gabon], "COUNTRY_ADMIN"));

    const sales = (await screen.findByText("Ventes (recharges facturées)")).closest("article") as HTMLElement;
    expect(text(sales)).toContain("138 FCFA");
    const commission = (await screen.findByText("Commission")).closest("article") as HTMLElement;
    expect(text(commission)).toContain("20 FCFA");
    expect(await screen.findByText("Argent de la plateforme")).toBeInTheDocument();
    expect(await screen.findByText(/Chiffres comptés depuis le/)).toBeInTheDocument();
    expect(screen.queryByRole("table", { name: "Détail par pays" })).not.toBeInTheDocument();
  });

  it("puts what needs attention in front: incidents, pending payments and live charges", async () => {
    navigation.search = new URLSearchParams("country=GA");
    serve([figures("GA", "Gabon", "XAF", 3)]);

    renderConsole(<OverviewPage />);

    const strip = await screen.findByRole("region", { name: "À surveiller" });
    expect(await within(strip).findByText("Incidents prioritaires")).toBeInTheDocument();
    expect(text(within(strip).getByText("Rechargements en attente").closest("a") as HTMLElement)).toContain("3");
    expect(text(within(strip).getByText("Recharges en cours").closest("a") as HTMLElement)).toContain("3");
    expect(within(strip).getByRole("link", { name: /Incidents prioritaires/ })).toHaveAttribute(
      "href",
      expect.stringContaining("severity=HIGH"),
    );
  });

  it("breaks the figures down by country when several are shown", async () => {
    serve([figures("GA", "Gabon", "XAF", 1), figures("ML", "Mali", "XOF", 2)]);

    renderConsole(<OverviewPage />);

    const table = await screen.findByRole("table", { name: "Détail par pays" });
    const rows = within(table).getAllByRole("row");
    expect(rows).toHaveLength(3);
    expect(within(rows[1]).getByText("Gabon")).toBeInTheDocument();
    expect(within(rows[2]).getByText("Mali")).toBeInTheDocument();
    expect(await screen.findByText(/mélange plusieurs devises/)).toBeInTheDocument();
  });

  it("offers a retry when the figures cannot be loaded", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ code: "FORBIDDEN" }, { status: 403 })));
    const user = userEvent.setup();

    renderConsole(<OverviewPage />);

    expect(await screen.findByRole("alert")).toHaveTextContent("Impossible de charger les indicateurs.");
    await user.click(screen.getByRole("button", { name: "Réessayer" }));
    expect(await screen.findByRole("alert")).toBeInTheDocument();
  });
});
