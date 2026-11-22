import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { UsageTab } from "./UsageTab";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/free-charging",
  useSearchParams: () => new URLSearchParams(),
}));

function use(index: number) {
  return { sessionId: `s${index}`, driverId: "d1", driverName: `Chauffeur ${index}`, startedAt: "2026-11-15T13:00:00Z", energyKwh: 18.4, stopReason: "EV_DISCONNECTED" };
}

function serve(total: number, items: unknown[]) {
  const fetchMock = vi.fn<typeof fetch>(async () => Response.json({ items, page: 0, size: 20, total }));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("UsageTab", () => {
  it("shows an empty state when no free session happened", async () => {
    serve(0, []);

    renderConsole(<UsageTab />, profileOf());

    expect(await screen.findByText("Aucune recharge offerte")).toBeInTheDocument();
  });

  it("lists the free sessions and pages through them", async () => {
    const fetchMock = serve(40, [use(1), use(2)]);
    const userEvents = userEvent.setup();
    renderConsole(<UsageTab />, profileOf());

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Chauffeur 1")).toBeInTheDocument();
    expect(within(table).getAllByText("18,4 kWh")).toHaveLength(2);
    await userEvents.click(screen.getByRole("button", { name: "Suivant" }));

    await waitFor(() => expect(fetchMock.mock.calls.some(([url]) => String(url) === "/api/admin/free-charging/usage?page=1&size=20")).toBe(true));
  });
});
