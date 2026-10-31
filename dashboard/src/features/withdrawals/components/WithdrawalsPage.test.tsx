import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import { WithdrawalsPage } from "./WithdrawalsPage";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams(), replace: vi.fn() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace }),
  usePathname: () => "/withdrawals",
  useSearchParams: () => navigation.search,
}));

const withdrawal = {
  kind: "WITHDRAWAL",
  id: "w1",
  country: "GA",
  customerId: "c1",
  customerName: "Aline Obame",
  operator: "AIRTEL",
  msisdn: "+24177123456",
  amountMinor: 4000,
  currency: "XAF",
  decimals: 0,
  state: "PENDING_APPROVAL",
  failureCode: null,
  reference: "GAW1",
  createdAt: "2026-10-28T15:36:00Z",
};

const reviewer = { ...profileOf([gabon]), permissions: ["OVERVIEW_READ", "PAYMENTS_READ", "WITHDRAWALS_REVIEW"] };

function serve(decision: () => Response = () => new Response(null, { status: 204 })) {
  const fetchMock = vi.fn<typeof fetch>(async (input, init) =>
    init?.method === "POST" ? decision() : Response.json({ total: 1, page: 0, size: 20, items: [withdrawal] }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

function decisions(fetchMock: ReturnType<typeof serve>) {
  return fetchMock.mock.calls.filter(([, init]) => init?.method === "POST");
}

describe("WithdrawalsPage", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams("country=GA");
    navigation.replace.mockClear();
  });

  it("lists only the withdrawals waiting for approval", async () => {
    const fetchMock = serve();

    renderConsole(<WithdrawalsPage />, reviewer);

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Aline Obame")).toBeInTheDocument();
    const url = String(fetchMock.mock.calls[0][0]);
    expect(url).toContain("kind=WITHDRAWAL");
    expect(url).toContain("state=PENDING_APPROVAL");
  });

  it("keeps the history one tab away, with every withdrawal and its state", async () => {
    navigation.search = new URLSearchParams("country=GA&tab=history");
    const fetchMock = serve();

    renderConsole(<WithdrawalsPage />, reviewer);

    const table = await screen.findByRole("table");
    expect(within(table).getByText("Aline Obame")).toBeInTheDocument();
    expect(within(table).getByText("À valider")).toBeInTheDocument();
    const url = String(fetchMock.mock.calls[0][0]);
    expect(url).toContain("kind=WITHDRAWAL");
    expect(url).not.toContain("state=PENDING_APPROVAL");
    expect(screen.getByRole("combobox", { name: "État" })).toBeInTheDocument();
  });

  it("switches between the waiting list and the history", async () => {
    serve();
    const user = userEvent.setup();
    renderConsole(<WithdrawalsPage />, reviewer);

    expect(screen.getByRole("button", { name: "À valider" })).toHaveAttribute("aria-pressed", "true");
    await user.click(screen.getByRole("button", { name: "Historique" }));

    expect(navigation.replace).toHaveBeenCalledWith(expect.stringContaining("tab=history"));
  });

  it("approves after a confirmation and tells the admin the payout is on its way", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<WithdrawalsPage />, reviewer);

    await user.click(await screen.findByRole("button", { name: "Valider" }));
    const dialog = screen.getByRole("dialog", { name: "Valider ce retrait ?" });
    expect(dialog).toHaveTextContent("Aline Obame");
    expect(decisions(fetchMock)).toHaveLength(0);
    await user.click(within(dialog).getByRole("button", { name: "Valider et envoyer" }));

    await waitFor(() => expect(decisions(fetchMock)).toHaveLength(1));
    expect(String(decisions(fetchMock)[0][0])).toBe("/api/admin/withdrawals/GA/w1/approve");
    expect(await screen.findByText("Retrait validé : l'envoi est en cours.")).toBeInTheDocument();
  });

  it("will not decline without a reason, then sends the reason", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<WithdrawalsPage />, reviewer);

    await user.click(await screen.findByRole("button", { name: "Refuser" }));
    const dialog = screen.getByRole("dialog", { name: "Refuser ce retrait ?" });
    await user.click(within(dialog).getByRole("button", { name: "Refuser le retrait" }));

    expect(within(dialog).getByText("Indiquez le motif du refus.")).toBeInTheDocument();
    expect(decisions(fetchMock)).toHaveLength(0);

    await user.type(within(dialog).getByLabelText("Motif du refus"), "Activité suspecte");
    await user.click(within(dialog).getByRole("button", { name: "Refuser le retrait" }));

    await waitFor(() => expect(decisions(fetchMock)).toHaveLength(1));
    expect(String(decisions(fetchMock)[0][0])).toBe("/api/admin/withdrawals/GA/w1/reject");
    expect(JSON.parse(String(decisions(fetchMock)[0][1]?.body))).toEqual({ note: "Activité suspecte" });
  });

  it("explains when someone else already handled the withdrawal", async () => {
    serve(() => Response.json({ code: "WITHDRAWAL_NOT_PENDING" }, { status: 409 }));
    const user = userEvent.setup();
    renderConsole(<WithdrawalsPage />, reviewer);

    await user.click(await screen.findByRole("button", { name: "Valider" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Valider et envoyer" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Ce retrait a déjà été traité.");
  });

  it("shows no decision buttons to an admin without the review permission", async () => {
    serve();

    renderConsole(<WithdrawalsPage />, { ...reviewer, permissions: ["OVERVIEW_READ", "PAYMENTS_READ"] });

    await screen.findByRole("table");
    expect(screen.queryByRole("button", { name: "Valider" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Refuser" })).not.toBeInTheDocument();
  });
});
