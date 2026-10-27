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
    },
  };
}

function card(name: string): Promise<HTMLElement> {
  return screen.findByRole("heading", { name, level: 3 }).then((heading) => heading.closest("article") as HTMLElement);
}

function serve(handler: (url: string) => Response | Promise<Response>) {
  const fetchMock = vi.fn(async (input: RequestInfo | URL) => handler(String(input)));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("OverviewPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams();
  });

  it("shows shimmer cards while the figures load", () => {
    serve(() => new Promise<Response>(() => undefined));

    renderConsole(<OverviewPage />);

    const loading = screen.getByRole("status");
    expect(loading).toHaveAttribute("aria-busy", "true");
    expect(loading.querySelectorAll(".skeleton").length).toBeGreaterThan(20);
  });

  it("asks the server for the selected country only", async () => {
    navigation.search = new URLSearchParams("country=GA");
    const fetchMock = serve(() =>
      Response.json({ scope: "GA", countries: [figures("GA", "Gabon", "XAF", 1)] }),
    );

    renderConsole(<OverviewPage />);

    expect(await card("Clients")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith("/api/admin/overview?country=GA", expect.anything());
  });

  it("shows the figures of one country in its own currency without a breakdown table", async () => {
    navigation.search = new URLSearchParams("country=GA");
    serve(() => Response.json({ scope: "GA", countries: [figures("GA", "Gabon", "XAF", 1)] }));

    renderConsole(<OverviewPage />, profileOf([gabon], "COUNTRY_ADMIN"));

    const revenue = (await screen.findByText("Chiffre d'affaires")).closest("article") as HTMLElement;
    expect(revenue.textContent?.replace(/[  ]/g, " ")).toContain("6 000 FCFA");
    const customers = screen.getByText("Clients").closest("article") as HTMLElement;
    expect(within(customers).getByText("10")).toBeInTheDocument();
    expect(within(customers).getByText(/8 actifs · 2 nouveaux/)).toBeInTheDocument();
    expect(screen.queryByRole("table")).not.toBeInTheDocument();
  });

  it("totals the counts across countries and keeps each currency on its own line", async () => {
    serve(() =>
      Response.json({
        scope: "ALL",
        countries: [figures("GA", "Gabon", "XAF", 1), figures("ML", "Mali", "XOF", 2)],
      }),
    );

    renderConsole(<OverviewPage />);

    const customers = await card("Clients");
    expect(within(customers).getByText("30")).toBeInTheDocument();
    const text = (await card("Chiffre d'affaires")).textContent?.replace(/[\u00a0\u202f]/g, " ") ?? "";
    expect(text).toContain("6 000 XAF");
    expect(text).toContain("12 000 XOF");
  });

  it("breaks the figures down by country when several are shown", async () => {
    serve(() =>
      Response.json({
        scope: "ALL",
        countries: [figures("GA", "Gabon", "XAF", 1), figures("ML", "Mali", "XOF", 2)],
      }),
    );

    renderConsole(<OverviewPage />);

    const table = await screen.findByRole("table", { name: "Détail par pays" });
    const rows = within(table).getAllByRole("row");
    expect(rows).toHaveLength(3);
    expect(within(rows[1]).getByText("Gabon")).toBeInTheDocument();
    expect(within(rows[2]).getByText("Mali")).toBeInTheDocument();
  });

  it("flags pending top-ups with a badge", async () => {
    serve(() => Response.json({ scope: "GA", countries: [figures("GA", "Gabon", "XAF", 3)] }));
    navigation.search = new URLSearchParams("country=GA");

    renderConsole(<OverviewPage />);

    const topups = await card("Recharges encaissées");
    expect(within(topups).getByText("3 en attente")).toBeInTheDocument();
  });

  it("offers a retry when the figures cannot be loaded", async () => {
    const fetchMock = serve(() => Response.json({ code: "FORBIDDEN" }, { status: 403 }));
    const user = userEvent.setup();

    renderConsole(<OverviewPage />);

    expect(await screen.findByRole("alert")).toHaveTextContent("Impossible de charger les indicateurs.");
    await user.click(screen.getByRole("button", { name: "Réessayer" }));
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
