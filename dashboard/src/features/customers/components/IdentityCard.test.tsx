import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { CustomerDetail } from "../schemas";
import { IdentityCard } from "./IdentityCard";

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
  email: "aline@example.com",
  phone: null,
  status: "ACTIVE",
  createdAt: "2026-10-01T10:00:00Z",
  emailVerified: true,
  phoneVerified: false,
  access: { lockedUntil: null, failedLoginAttempts: 0, pendingEmail: null, pendingPhone: null },
  ndzela: { linked: false, idTag: null },
  wallet: null,
  sessions: { completed: 0, live: 0, energyKwh: 0, spentMinor: 0, lastSessionAt: null },
};

describe("IdentityCard", () => {
  it("offers the name edit to a super admin only", () => {
    renderConsole(<IdentityCard customer={customer} />, { ...profileOf(), role: "ADMIN" });

    expect(screen.queryByRole("button", { name: "Modifier le nom" })).not.toBeInTheDocument();
  });

  it("saves the new name typed by the super admin", async () => {
    const fetchMock = vi.fn<typeof fetch>(async () => new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderConsole(<IdentityCard customer={customer} />, { ...profileOf(), role: "SUPER_ADMIN" });

    await user.click(screen.getByRole("button", { name: "Modifier le nom" }));
    await user.clear(screen.getByLabelText("Prénom"));
    await user.type(screen.getByLabelText("Prénom"), "Marie");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1/name");
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ firstName: "Marie", lastName: "Obame" });
  });

  it("shows every way this customer signs in", () => {
    renderConsole(<IdentityCard customer={{ ...customer, signInMethods: ["PASSWORD", "APPLE", "GOOGLE"] }} />, profileOf());

    expect(screen.getByText("Connexion")).toBeInTheDocument();
    expect(screen.getByText("Mot de passe (e-mail ou téléphone)")).toBeInTheDocument();
    expect(screen.getByText("Apple")).toBeInTheDocument();
    expect(screen.getByText("Google")).toBeInTheDocument();
  });

  it("hides the password change from roles other than admin and super admin", () => {
    renderConsole(<IdentityCard customer={customer} />, { ...profileOf(), role: "SUPPORT", permissions: ["CUSTOMERS_READ", "CUSTOMERS_MANAGE"] });

    expect(screen.queryByRole("button", { name: "Changer le mot de passe" })).not.toBeInTheDocument();
  });

  it("lets an admin set a new password only once both entries match", async () => {
    const fetchMock = vi.fn<typeof fetch>(async () => new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderConsole(<IdentityCard customer={customer} />, { ...profileOf(), role: "ADMIN", permissions: ["CUSTOMERS_READ", "CUSTOMERS_MANAGE"] });

    await user.click(screen.getByRole("button", { name: "Changer le mot de passe" }));
    await user.type(screen.getByLabelText("Nouveau mot de passe"), "Nouveau-Pass-2026");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "Nouveau-Pass");
    expect(screen.getByRole("button", { name: "Changer" })).toBeDisabled();
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "-2026");
    await user.click(screen.getByRole("button", { name: "Changer" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1/password");
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ password: "Nouveau-Pass-2026" });
  });
});
