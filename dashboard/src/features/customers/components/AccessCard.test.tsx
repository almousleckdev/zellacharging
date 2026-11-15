import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import type { CustomerDetail } from "../schemas";
import { AccessCard } from "./AccessCard";
import { NdzelaCard } from "./NdzelaCard";

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
  phone: "+24106123456",
  status: "ACTIVE",
  createdAt: "2026-10-01T10:00:00Z",
  emailVerified: true,
  phoneVerified: false,
  access: { lockedUntil: "2026-11-15T21:00:00Z", failedLoginAttempts: 0, pendingEmail: null, pendingPhone: null },
  ndzela: { linked: false, idTag: null },
  wallet: null,
  sessions: { completed: 0, live: 0, energyKwh: 0, spentMinor: 0, lastSessionAt: null },
};

const manager = { ...profileOf(), permissions: ["OVERVIEW_READ", "CUSTOMERS_MANAGE"] };

function serve() {
  const fetchMock = vi.fn<typeof fetch>(async () => new Response(null, { status: 204 }));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("AccessCard", () => {
  it("shows no action to an admin who cannot manage customers", () => {
    renderConsole(<AccessCard customer={customer} />, { ...profileOf(), permissions: ["OVERVIEW_READ"] });

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("unlocks the sign-in after a confirmation", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<AccessCard customer={customer} />, manager);

    await user.click(screen.getByRole("button", { name: "Débloquer la connexion" }));
    await user.click(screen.getAllByRole("button", { name: "Débloquer la connexion" }).at(-1) as HTMLElement);

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1/unlock");
    expect(fetchMock.mock.calls[0][1]?.method).toBe("POST");
  });

  it("resends the code on the unverified phone, by the chosen delivery", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<AccessCard customer={customer} />, manager);

    await user.click(screen.getByRole("button", { name: "Renvoyer le code" }));
    await user.selectOptions(screen.getByLabelText("Envoyer par"), "SMS");
    await user.click(screen.getByRole("button", { name: "Envoyer le code" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1/resend-code");
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ channel: "PHONE", delivery: "SMS" });
  });

  it("changes the email with the typed value", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<AccessCard customer={customer} />, manager);

    await user.click(screen.getByRole("button", { name: "Changer le contact" }));
    await user.selectOptions(screen.getByLabelText("Contact"), "EMAIL");
    await user.type(screen.getByLabelText("Nouvelle valeur"), "new@example.com");
    await user.click(screen.getAllByRole("button", { name: "Changer" }).at(-1) as HTMLElement);

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(fetchMock.mock.calls[0][1]?.method).toBe("PUT");
    expect(JSON.parse(String(fetchMock.mock.calls[0][1]?.body))).toEqual({ channel: "EMAIL", value: "new@example.com" });
  });
});

describe("NdzelaCard", () => {
  it("links the customer to NDZela after a confirmation", async () => {
    const fetchMock = serve();
    const user = userEvent.setup();
    renderConsole(<NdzelaCard customer={customer} />, manager);

    await user.click(screen.getByRole("button", { name: "Lier à NDZela" }));
    await user.click(screen.getAllByRole("button", { name: "Lier à NDZela" }).at(-1) as HTMLElement);

    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    expect(String(fetchMock.mock.calls[0][0])).toBe("/api/admin/customers/GA/c1/ndzela-link");
    expect(fetchMock.mock.calls[0][1]?.method).toBe("PUT");
  });
});
