import { screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { CommissionsPage } from "./CommissionsPage";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/commissions",
  useSearchParams: () => navigation.search,
}));

const summary = {
  country: "GA",
  currency: "XAF",
  decimals: 0,
  sessions: 2,
  electricMinor: 735,
  serviceFeeMinor: 15,
  vatMinor: 0,
  serviceFeePercent: 2,
  vatPercent: 0,
};

const item = {
  sessionId: "s1",
  country: "GA",
  customerName: "Aline Obame",
  stationId: "CP-12",
  electricMinor: 135,
  serviceFeeMinor: 3,
  vatMinor: 0,
  totalMinor: 138,
  currency: "XAF",
  decimals: 0,
  at: "2026-10-28T15:48:00Z",
};

describe("CommissionsPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams("country=GA");
  });

  it("shows the commission earned and the fee of each billed charge", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ summaries: [summary], total: 1, page: 0, size: 20, items: [item] })));

    renderConsole(<CommissionsPage />);

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Aline Obame")).toBeInTheDocument();
    expect(within(table).getByText(/138/)).toBeInTheDocument();
    const earned = (await screen.findByText("Commission gagnée (2 %)")).closest("article") as HTMLElement;
    expect(earned.textContent?.replace(/[  ]/g, " ")).toContain("15 FCFA");
    expect(screen.getByRole("columnheader", { name: "TVA" })).toBeInTheDocument();
  });

  it("explains an empty list", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ summaries: [{ ...summary, sessions: 0 }], total: 0, page: 0, size: 20, items: [] })));

    renderConsole(<CommissionsPage />);

    expect(await screen.findByText("Aucune commission")).toBeInTheDocument();
  });
});
