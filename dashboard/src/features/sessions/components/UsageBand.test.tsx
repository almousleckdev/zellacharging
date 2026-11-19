import { screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { UsageBand } from "./UsageBand";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/sessions",
  useSearchParams: () => new URLSearchParams("country=GA"),
}));

function totals(sessions: number, energyKwh: number, collectedMinor: number, staffEnergyKwh = 0) {
  return { sessions, energyKwh, collectedMinor, staffEnergyKwh };
}

describe("UsageBand", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () =>
        Response.json({
          countries: [
            {
              code: "GA",
              name: "Gabon",
              currency: "XAF",
              decimals: 0,
              day: "2026-11-19",
              today: totals(2, 43.9, 4564),
              yesterday: totals(18, 380.5, 332913, 12.5),
              week: totals(40, 800, 949066),
              month: totals(60, 1200, 988558),
            },
          ],
        }),
      ),
    );
  });

  it("shows charges, energy and collected money for today, yesterday, the week and the month", async () => {
    renderConsole(<UsageBand scope={{ station: "CP-12" }} />);

    const today = (await screen.findByText("Aujourd'hui")).closest("article") as HTMLElement;
    expect(within(today).getByText("2")).toBeInTheDocument();
    expect(within(today).getByText(/43,9/)).toBeInTheDocument();
    expect(within(today).getByText(/4\s564/)).toBeInTheDocument();
    const yesterday = screen.getByText("Hier").closest("article") as HTMLElement;
    expect(within(yesterday).getByText(/332\s913/)).toBeInTheDocument();
    expect(within(yesterday).getByText(/12,5/)).toBeInTheDocument();
    expect(screen.getByText("Cette semaine")).toBeInTheDocument();
    expect(screen.getByText("Ce mois-ci")).toBeInTheDocument();
  });
});
