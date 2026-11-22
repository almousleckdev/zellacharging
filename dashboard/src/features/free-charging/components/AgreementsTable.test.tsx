import { screen, within } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { Agreement } from "../schemas";
import { AgreementsTable } from "./AgreementsTable";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/free-charging",
  useSearchParams: () => new URLSearchParams(),
}));

function agreement(id: string, eligibility: string): Agreement {
  return { id, driverId: `driver-${id}`, driverName: `Driver ${id}`, startsOn: "2026-11-01", endsOn: null, status: "ACTIVE", conditions: null, eligibility, unpaidDays: 0, lastFreeUse: null };
}

describe("AgreementsTable eligibility", () => {
  it("labels the Zella reasons and never shows a raw unknown code", () => {
    const items = [agreement("a", "SUNDAY_USED"), agreement("b", "UNAVAILABLE"), agreement("c", "SOMETHING_NEW")];

    renderConsole(<AgreementsTable items={items} onAction={vi.fn()} />, { ...profileOf(), permissions: ["FREE_CHARGING_READ"] });

    const table = screen.getByRole("table");
    expect(within(table).getByText("Déjà utilisée ce dimanche")).toBeInTheDocument();
    expect(within(table).getByText("Indisponible")).toBeInTheDocument();
    expect(within(table).getByText("Statut inconnu")).toBeInTheDocument();
    expect(within(table).queryByText("SOMETHING_NEW")).not.toBeInTheDocument();
  });
});
