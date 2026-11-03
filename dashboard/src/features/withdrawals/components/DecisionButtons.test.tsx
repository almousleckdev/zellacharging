import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { WithdrawalItem } from "../schemas";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import { DecisionButtons } from "./DecisionButtons";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/payments",
  useSearchParams: () => new URLSearchParams("country=GA"),
}));

const stuck: WithdrawalItem = {
  kind: "WITHDRAWAL",
  id: "w2",
  country: "GA",
  customerId: "c1",
  customerName: "Aline Obame",
  operator: "AIRTEL",
  msisdn: "+24177123456",
  amountMinor: 1000,
  currency: "XAF",
  decimals: 0,
  state: "REQUESTED",
  failureCode: null,
  reference: "GAW2",
  createdAt: "2026-10-31T10:40:00Z",
};

const reviewer = { ...profileOf([gabon]), permissions: ["OVERVIEW_READ", "PAYMENTS_READ", "WITHDRAWALS_REVIEW"] };

function serve(answer: () => Response = () => new Response(null, { status: 204 })) {
  const fetchMock = vi.fn<typeof fetch>(async () => answer());
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("DecisionButtons", () => {
  beforeEach(() => vi.unstubAllGlobals());

  it("offers a refund, not approve or decline, on a withdrawal stuck after approval", () => {
    renderConsole(<DecisionButtons withdrawal={stuck} />, reviewer);

    expect(screen.getByRole("button", { name: "Rembourser" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Valider" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Refuser" })).not.toBeInTheDocument();
  });

  it("offers nothing on a finished withdrawal", () => {
    renderConsole(<DecisionButtons withdrawal={{ ...stuck, state: "PAID" }} />, reviewer);

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("offers nothing without the review permission", () => {
    renderConsole(<DecisionButtons withdrawal={stuck} />, { ...reviewer, permissions: ["PAYMENTS_READ"] });

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("will not refund without a reason, then sends the reason", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<DecisionButtons withdrawal={stuck} />, reviewer);

    await user.click(screen.getByRole("button", { name: "Rembourser" }));
    const dialog = screen.getByRole("dialog", { name: "Rembourser ce retrait ?" });
    await user.click(within(dialog).getByRole("button", { name: "Rembourser le client" }));

    expect(within(dialog).getByText("Indiquez le motif du remboursement.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();

    await user.type(within(dialog).getByLabelText("Motif du remboursement"), "Refusé par l'opérateur");
    await user.click(within(dialog).getByRole("button", { name: "Rembourser le client" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/withdrawals/GA/w2/refund");
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ note: "Refusé par l'opérateur" });
    expect(await screen.findByText("Retrait remboursé : le montant est revenu dans le portefeuille.")).toBeInTheDocument();
  });

  it("explains when the operator already has the payout", async () => {
    serve(() => Response.json({ code: "WITHDRAWAL_PAYOUT_SENT" }, { status: 409 }));
    const user = userEvent.setup();
    renderConsole(<DecisionButtons withdrawal={stuck} />, reviewer);

    await user.click(screen.getByRole("button", { name: "Rembourser" }));
    const dialog = screen.getByRole("dialog");
    await user.type(within(dialog).getByLabelText("Motif du remboursement"), "Test");
    await user.click(within(dialog).getByRole("button", { name: "Rembourser le client" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("L'opérateur a reçu ce retrait");
  });
});
