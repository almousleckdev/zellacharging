import { screen, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { PaymentsPage } from "./PaymentsPage";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/payments",
  useSearchParams: () => navigation.search,
}));

function payment(kind: string, state: string, amountMinor: number) {
  return {
    kind,
    id: `${kind}-1`,
    country: "GA",
    customerId: "c1",
    customerName: "Aline Obame",
    operator: "AIRTEL",
    msisdn: "+24177123456",
    amountMinor,
    currency: "XAF",
    decimals: 0,
    state,
    failureCode: null,
    reference: `GA${kind}`,
    createdAt: "2026-10-28T15:36:00Z",
  };
}

describe("PaymentsPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams("country=GA");
  });

  it("lists top-ups and withdrawals with their state and signed amount", async () => {
    const fetchMock = vi.fn<typeof fetch>(async () =>
      Response.json({ total: 2, page: 0, size: 20, items: [payment("TOPUP", "SUCCEEDED", 2000), payment("WITHDRAWAL", "PAID", 500)] }),
    );
    vi.stubGlobal("fetch", fetchMock);

    renderConsole(<PaymentsPage />);

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Recharge")).toBeInTheDocument();
    expect(within(table).getByText("Réussie")).toBeInTheDocument();
    expect(within(table).getByText("Versé")).toBeInTheDocument();
    expect(within(table).getByText(/^\+\s/)).toBeInTheDocument();
    expect(within(table).getAllByRole("link", { name: /Aline Obame/ })[0]).toHaveAttribute("href", "/customers/GA/c1?country=GA");
    expect(screen.getByText("2 opérations")).toBeInTheDocument();
    expect(String(fetchMock.mock.calls[0][0])).toContain("/api/admin/payments?country=GA");
  });

  it("explains an empty list", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => Response.json({ total: 0, page: 0, size: 20, items: [] })));

    renderConsole(<PaymentsPage />);

    expect(await screen.findByText("Aucun paiement")).toBeInTheDocument();
  });
});
