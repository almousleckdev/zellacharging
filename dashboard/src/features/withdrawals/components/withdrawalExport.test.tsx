import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { ExportSheet } from "@/shared/export/types";
import type { ListFilters } from "@/shared/lists/filters";
import { useExportSheet } from "@/shared/lists/useExportSheet";
import type { WithdrawalItem } from "../schemas";
import { useWithdrawalColumns } from "./useWithdrawalColumns";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/withdrawals",
  useSearchParams: () => new URLSearchParams(),
}));

const filters: ListFilters = { country: "ALL", text: "", from: "", to: "", page: 0, size: 20, sort: "", extra: {} };

const withdrawal: WithdrawalItem = {
  kind: "WITHDRAWAL",
  id: "w1",
  country: "GA",
  customerId: "c1",
  customerName: "Aïcha Traoré",
  operator: "AIRTEL",
  msisdn: "+24177000111",
  amountMinor: 45000,
  currency: "XAF",
  decimals: 0,
  state: "PAID",
  failureCode: null,
  reference: "WD-1",
  createdAt: "2026-10-01T10:00:00Z",
};

function Probe({ onSheet }: { onSheet: (sheet: ExportSheet<WithdrawalItem>) => void }) {
  const columns = useWithdrawalColumns();
  onSheet(useExportSheet("Retraits", "retraits", columns, filters)([withdrawal]));
  return null;
}

function sheetOfWithdrawals(): ExportSheet<WithdrawalItem> {
  let sheet: ExportSheet<WithdrawalItem> | undefined;
  renderConsole(<Probe onSheet={(value) => (sheet = value)} />, profileOf());
  return sheet as ExportSheet<WithdrawalItem>;
}

describe("the withdrawals export", () => {
  it("separates the customer, the operator and the phone number", () => {
    const { columns, rows } = sheetOfWithdrawals();
    const values = (header: string) => columns.find((column) => column.header === header)?.value(rows[0]);

    expect(values("Client")).toBe("Aïcha Traoré");
    expect(values("Opérateur")).toBe("AIRTEL");
    expect(values("Téléphone")).toBe("+24177000111");
  });
});
