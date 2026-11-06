import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { ExportSheet } from "@/shared/export/types";
import type { ListFilters } from "@/shared/lists/filters";
import { useExportSheet } from "@/shared/lists/useExportSheet";
import type { CustomerItem } from "../schemas";
import { useCustomerColumns } from "./useCustomerColumns";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/customers",
  useSearchParams: () => new URLSearchParams(),
}));

const filters: ListFilters = { country: "ALL", text: "", from: "", to: "", page: 0, size: 20, sort: "", extra: {} };

const customer: CustomerItem = {
  id: "c1",
  country: "GA",
  firstName: "Aïcha",
  lastName: "Traoré",
  email: "aicha@example.com",
  phone: "+24177000111",
  status: "ACTIVE",
  createdAt: "2026-10-01T10:00:00Z",
  balanceMinor: 1000,
  currency: "XAF",
  decimals: 0,
  completedSessions: 4,
  spentMinor: 38000,
  energyKwh: 12,
  lastSessionAt: null,
  avatarVersion: null,
};

function Probe({ onSheet }: { onSheet: (sheet: ExportSheet<CustomerItem>) => void }) {
  const columns = useCustomerColumns();
  const build = useExportSheet("Clients", "clients", columns, filters);
  onSheet(build([customer]));
  return null;
}

function sheetOfCustomers(): ExportSheet<CustomerItem> {
  let sheet: ExportSheet<CustomerItem> | undefined;
  renderConsole(<Probe onSheet={(value) => (sheet = value)} />, profileOf());
  return sheet as ExportSheet<CustomerItem>;
}

describe("the customers export", () => {
  it("puts the first name, the last name, the e-mail and the phone in their own columns", () => {
    const { columns, rows } = sheetOfCustomers();
    const values = (header: string) => columns.find((column) => column.header === header)?.value(rows[0]);

    expect(values("Prénom")).toBe("Aïcha");
    expect(values("Nom")).toBe("Traoré");
    expect(values("E-mail")).toBe("aicha@example.com");
    expect(values("Téléphone")).toBe("+24177000111");
  });

  it("has no merged customer column any more", () => {
    const headers = sheetOfCustomers().columns.map((column) => column.header);

    expect(headers).not.toContain("Client");
    expect(headers.slice(0, 4)).toEqual(["Prénom", "Nom", "E-mail", "Téléphone"]);
  });

  it("keeps the other columns", () => {
    const headers = sheetOfCustomers().columns.map((column) => column.header);

    expect(headers).toEqual(expect.arrayContaining(["Statut", "Recharges", "Dépensé", "Dernière recharge", "Inscrit le"]));
  });

  it("leaves an empty cell, not the word undefined, when a customer has no e-mail or phone", () => {
    const { columns } = sheetOfCustomers();
    const email = columns.find((column) => column.header === "E-mail")!;

    expect(email.value({ ...customer, email: null })).toBe("");
  });
});
