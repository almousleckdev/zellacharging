import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { CustomerDetail } from "../schemas";
import { NdzelaCard } from "./NdzelaCard";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/customers/GA/c1",
  useSearchParams: () => new URLSearchParams("country=GA"),
  useParams: () => ({ country: "GA", id: "c1" }),
}));

const linked: CustomerDetail = {
  id: "c1",
  country: "GA",
  firstName: "Aline",
  lastName: "Obame",
  email: "aline@example.com",
  phone: "+24106123456",
  status: "ACTIVE",
  createdAt: "2026-10-01T10:00:00Z",
  emailVerified: true,
  phoneVerified: true,
  access: { lockedUntil: null, failedLoginAttempts: 0, pendingEmail: null, pendingPhone: null },
  ndzela: { linked: true, idTag: "Z-ABC" },
  wallet: null,
  sessions: { completed: 0, live: 0, energyKwh: 0, spentMinor: 0, lastSessionAt: null },
};

const reader = { ...profileOf(), permissions: ["OVERVIEW_READ", "CUSTOMERS_READ"] };

const profile = {
  driver: { firstName: "Aline", lastName: "Obame", phone: "+24106123456", email: "aline@example.com", status: "ACTIVE" },
  contract: { number: "NDZ-GA-0042", status: "ACTIVE", startDate: "2026-11-01", endDate: "2027-05-01", dailyRent: 23000 },
  car: { fleetNumber: "V001", plate: "GA-123-AB", brand: "BYD", model: "Dolphin", year: 2025, color: "Blanc" },
};

describe("NdzelaDriverDetails", () => {
  it("shows the contract number and the car number of a linked driver", async () => {
    const fetchMock = vi.fn<typeof fetch>(async () => Response.json(profile));
    vi.stubGlobal("fetch", fetchMock);

    renderConsole(<NdzelaCard customer={linked} />, reader);

    expect(await screen.findByText("NDZ-GA-0042")).toBeInTheDocument();
    expect(screen.getByText("V001")).toBeInTheDocument();
    expect(screen.getByText("GA-123-AB")).toBeInTheDocument();
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1/ndzela-driver");
  });

  it("does not ask NDZela about a customer who is not linked", () => {
    const fetchMock = vi.fn<typeof fetch>(async () => Response.json(profile));
    vi.stubGlobal("fetch", fetchMock);

    renderConsole(<NdzelaCard customer={{ ...linked, ndzela: { linked: false, idTag: null } }} />, reader);

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("tells the admin when NDZela cannot be reached and offers to try again", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ code: "NDZELA_UNAVAILABLE", status: 503 }, { status: 503 })));

    renderConsole(<NdzelaCard customer={linked} />, reader);

    expect(await screen.findByText(/NDZela est momentanément injoignable/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Réessayer" })).toBeInTheDocument();
  });
});
