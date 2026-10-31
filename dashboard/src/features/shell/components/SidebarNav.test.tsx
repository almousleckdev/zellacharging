import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { SidebarNav } from "./SidebarNav";

vi.mock("next/navigation", () => ({
  usePathname: () => "/withdrawals",
  useRouter: () => ({ replace: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
}));

describe("SidebarNav", () => {
  it("lists the withdrawals for an admin who can review them and marks the open page", () => {
    renderConsole(<SidebarNav />, { ...profileOf(), permissions: ["OVERVIEW_READ", "WITHDRAWALS_REVIEW"] });

    expect(screen.getByRole("link", { name: "Retraits" })).toHaveAttribute("aria-current", "page");
  });

  it("hides the withdrawals from an admin who cannot review them", () => {
    renderConsole(<SidebarNav />, { ...profileOf(), permissions: ["OVERVIEW_READ", "PAYMENTS_READ"] });

    expect(screen.queryByRole("link", { name: "Retraits" })).not.toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Paiements" })).toBeInTheDocument();
  });
});
