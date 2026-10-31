import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import { SettingsCard } from "./SettingsCard";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/tariffs",
  useSearchParams: () => new URLSearchParams(),
}));

const wallet = {
  section: "wallet",
  values: {
    minimumStartMinor: 2000,
    maximumStartMinor: 200000,
    minTopupMinor: 500,
    maxTopupMinor: 500000,
    withdrawalsEnabled: false,
    withdrawalMinimumMinor: 500,
    withdrawalMaximumMinor: 200000,
    withdrawalDailyMaximumMinor: 500000,
    paymentProvider: "sandbox",
    sandboxAllowed: true,
  },
};

const superAdmin = { ...profileOf([gabon]), permissions: ["OVERVIEW_READ", "ADMINS_MANAGE"] };

function serve(answer: () => Response = () => Response.json(wallet)) {
  const fetchMock = vi.fn<typeof fetch>(async () => answer());
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("SettingsCard", () => {
  beforeEach(() => vi.unstubAllGlobals());

  it("shows the values read-only to an admin who cannot manage settings", () => {
    renderConsole(<SettingsCard section={wallet} currency="XAF" decimals={0} />, { ...superAdmin, permissions: ["OVERVIEW_READ"] });

    expect(screen.queryByRole("button", { name: "Modifier" })).not.toBeInTheDocument();
    expect(screen.getByText("Rechargement minimum")).toBeInTheDocument();
  });

  it("lets the super admin edit a limit and sends only what changed", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<SettingsCard section={wallet} currency="XAF" decimals={0} />, superAdmin);

    await user.click(screen.getByRole("button", { name: "Modifier" }));
    const field = screen.getByLabelText("Rechargement minimum");
    await user.clear(field);
    await user.type(field, "1000");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/settings/wallet");
    expect(fetchMock.mock.calls[0][1]?.method).toBe("PUT");
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ values: { minTopupMinor: 1000 } });
    expect(await screen.findByText("Réglages enregistrés.")).toBeInTheDocument();
  });

  it("switches withdrawals on with the checkbox", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<SettingsCard section={wallet} currency="XAF" decimals={0} />, superAdmin);

    await user.click(screen.getByRole("button", { name: "Modifier" }));
    await user.click(screen.getByLabelText("Retraits"));
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ values: { withdrawalsEnabled: true } });
  });

  it("explains a refused value and keeps the form open", async () => {
    serve(() => Response.json({ code: "SETTING_RANGE_INVALID" }, { status: 400 }));
    const user = userEvent.setup();
    renderConsole(<SettingsCard section={wallet} currency="XAF" decimals={0} />, superAdmin);

    await user.click(screen.getByRole("button", { name: "Modifier" }));
    const field = screen.getByLabelText("Rechargement minimum");
    await user.clear(field);
    await user.type(field, "900000");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    expect(await screen.findByText("Le minimum ne peut pas dépasser le maximum.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Enregistrer" })).toBeInTheDocument();
  });

  it("does not offer to edit a section the backend does not allow editing", () => {
    renderConsole(<SettingsCard section={{ section: "reporting", values: { reportingStart: null } }} currency="XAF" decimals={0} />, superAdmin);

    expect(screen.queryByRole("button", { name: "Modifier" })).not.toBeInTheDocument();
  });
});
