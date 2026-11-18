import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { CustomerDetail } from "../schemas";
import { FreeAccessCard } from "./FreeAccessCard";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/customers/GA/c1",
  useSearchParams: () => new URLSearchParams("country=GA"),
  useParams: () => ({ country: "GA", id: "c1" }),
}));

const customer: CustomerDetail = {
  id: "c1",
  country: "GA",
  firstName: "Aline",
  lastName: "Obame",
  email: null,
  phone: null,
  status: "ACTIVE",
  createdAt: "2026-10-01T10:00:00Z",
  emailVerified: true,
  access: { lockedUntil: null, failedLoginAttempts: 0, pendingEmail: null, pendingPhone: null },
  ndzela: { linked: false, idTag: null },
  phoneVerified: false,
  wallet: null,
  sessions: { completed: 0, live: 0, energyKwh: 0, spentMinor: 0, lastSessionAt: null },
};

const granted: CustomerDetail = {
  ...customer,
  freeAccess: { reason: "PDG", category: "OTHER", grantedAt: "2026-11-12T10:00:00Z", expiresAt: null },
};

const manager = { ...profileOf(), permissions: ["OVERVIEW_READ", "COMPLIMENTARY_MANAGE"] };

function serve() {
  const fetchMock = vi.fn<typeof fetch>(async () => new Response(null, { status: 204 }));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("FreeAccessCard", () => {
  it("stays hidden from an admin who cannot manage free charging when the customer has none", () => {
    renderConsole(<FreeAccessCard customer={customer} />, { ...profileOf(), permissions: ["OVERVIEW_READ"] });

    expect(screen.queryByRole("heading", { name: "Recharge gratuite" })).not.toBeInTheDocument();
  });

  it("shows the reason to a viewer but no button to change it", () => {
    renderConsole(<FreeAccessCard customer={granted} />, { ...profileOf(), permissions: ["OVERVIEW_READ"] });

    expect(screen.getByText("Motif: PDG")).toBeInTheDocument();
    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("gives free charging with the reason typed by the admin", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<FreeAccessCard customer={customer} />, manager);

    await user.click(screen.getByRole("button", { name: "Accorder la recharge gratuite" }));
    await user.type(screen.getByLabelText("Motif (par exemple PDG)"), "DG");
    await user.click(screen.getByRole("button", { name: "Accorder l'accès" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    const [url, init] = fetchMock.mock.calls[0];
    expect(String(url)).toBe("/api/admin/customers/GA/c1/complimentary");
    expect(init?.method).toBe("PUT");
    expect(JSON.parse(String(init?.body))).toEqual({ reason: "DG", category: "NDZELA_STAFF", expiresAt: null });
  });

  it("lets the admin choose another type of access", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<FreeAccessCard customer={customer} />, manager);

    await user.click(screen.getByRole("button", { name: "Accorder la recharge gratuite" }));
    await user.selectOptions(screen.getByLabelText("Type d'accès"), "OTHER");
    await user.type(screen.getByLabelText("Motif (par exemple PDG)"), "Invité");
    await user.click(screen.getByRole("button", { name: "Accorder l'accès" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toMatchObject({ category: "OTHER" });
  });

  it("removes free charging after a confirmation", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<FreeAccessCard customer={granted} />, manager);

    await user.click(screen.getByRole("button", { name: "Retirer la recharge gratuite" }));
    await user.click(screen.getAllByRole("button", { name: "Retirer la recharge gratuite" }).at(-1) as HTMLElement);

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(fetchMock.mock.calls[0][1]?.method).toBe("DELETE");
  });
});
