import { screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { SessionsByCustomerPage } from "./SessionsByCustomerPage";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/sessions",
  useSearchParams: () => navigation.search,
}));

const last = {
  id: "s9",
  country: "GA",
  customerId: "c1",
  customerName: "Almousleck Ag",
  stationId: "CP-12",
  connectorId: 2,
  state: "COMPLETED",
  failureCode: null,
  energyKwh: 2,
  costMinor: 500,
  currency: "XAF",
  decimals: 0,
  startBatteryPercent: null,
  endBatteryPercent: null,
  startedAt: "2026-10-28T15:46:00Z",
  stoppedAt: "2026-10-28T15:48:00Z",
  createdAt: "2026-10-28T15:45:00Z",
};

describe("SessionsByCustomerPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams("country=GA");
  });

  it("lists a customer once with the number of charges, the last charge and the total, asking for ten rows", async () => {
    const fetchMock = vi.fn(async () =>
      Response.json({
        total: 1,
        page: 0,
        size: 10,
        items: [{ customerId: "c1", country: "GA", customerName: "Almousleck Ag", sessions: 100, energyKwh: 200, spentMinor: 50000, last }],
      }),
    );
    vi.stubGlobal("fetch", fetchMock);

    renderConsole(<SessionsByCustomerPage />);

    const table = await screen.findByRole("table");
    expect(within(table).getAllByText("Almousleck Ag")).toHaveLength(1);
    expect(within(table).getByText("100")).toBeInTheDocument();
    expect(within(table).getByText("CP-12")).toBeInTheDocument();
    expect(within(table).getByText(/50\s000/)).toBeInTheDocument();
    const url = String((fetchMock.mock.calls[0] as unknown[])[0]);
    expect(url).toContain("/api/admin/sessions/by-customer?");
    expect(url).toContain("size=10");
  });
});
