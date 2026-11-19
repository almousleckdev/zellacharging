import { screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { CustomerCountsBand } from "./CustomerCountsBand";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/customers",
  useSearchParams: () => new URLSearchParams("country=GA"),
}));

describe("CustomerCountsBand", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () => Response.json({ total: 120, publicUsers: 100, ndzelaDrivers: 15, ndzelaStaff: 5, suspended: 3 })),
    );
  });

  it("splits the customers between public, NDZela drivers and NDZela staff", async () => {
    renderConsole(<CustomerCountsBand />);

    const total = (await screen.findByText("Clients")).closest("article") as HTMLElement;
    expect(within(total).getByText("120")).toBeInTheDocument();
    expect(within(screen.getByText("Public").closest("article") as HTMLElement).getByText("100")).toBeInTheDocument();
    expect(within(screen.getByText("Chauffeurs NDZela").closest("article") as HTMLElement).getByText("15")).toBeInTheDocument();
    expect(within(screen.getByText("Personnel NDZela").closest("article") as HTMLElement).getByText("5")).toBeInTheDocument();
    expect(within(screen.getByText("Suspendus").closest("article") as HTMLElement).getByText("3")).toBeInTheDocument();
  });
});
