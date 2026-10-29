import { screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { SessionsPage } from "./SessionsPage";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/sessions",
  useSearchParams: () => navigation.search,
}));

const session = {
  id: "s1",
  country: "GA",
  customerId: "c1",
  customerName: "Aline Obame",
  stationId: "CP-12",
  connectorId: 2,
  state: "COMPLETED",
  failureCode: null,
  energyKwh: 0.385,
  costMinor: 138,
  currency: null,
  decimals: null,
  startBatteryPercent: 22,
  endBatteryPercent: 64,
  startedAt: "2026-10-28T15:46:00Z",
  stoppedAt: "2026-10-28T15:48:00Z",
  createdAt: "2026-10-28T15:46:00Z",
};

describe("SessionsPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams("country=GA");
  });

  it("shows the station, the energy and the amount of each session", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ total: 1, page: 0, size: 20, items: [session] })));

    renderConsole(<SessionsPage />);

    const table = await screen.findByRole("table");
    expect(within(table).getByText("CP-12")).toBeInTheDocument();
    expect(within(table).getByText("Terminée")).toBeInTheDocument();
    expect(within(table).getByText("0,39 kWh")).toBeInTheDocument();
    expect(screen.getByText("1 clients")).toBeInTheDocument();
  });

  it("shows dashes while a session has no figures yet", async () => {
    const live = { ...session, state: "ACTIVE", energyKwh: null, costMinor: null, startBatteryPercent: null, endBatteryPercent: null };
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ total: 1, page: 0, size: 20, items: [live] })));

    renderConsole(<SessionsPage />);

    const table = await screen.findByRole("table");
    expect(within(table).getByText("En charge")).toBeInTheDocument();
    expect(within(table).getAllByText("–").length).toBeGreaterThanOrEqual(2);
  });
});
