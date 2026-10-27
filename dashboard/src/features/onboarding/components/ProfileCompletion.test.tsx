import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { QueryProvider } from "@/shared/api/QueryProvider";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
import { ToastProvider } from "@/shared/ui/ToastProvider";
import { render } from "@testing-library/react";
import { AppShell } from "@/features/shell/components/AppShell";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/",
  useSearchParams: () => new URLSearchParams(),
}));

const incomplete = {
  id: "1",
  email: "new@zella.test",
  firstName: null,
  lastName: null,
  phone: null,
  role: "ADMIN",
  permissions: ["OVERVIEW_READ"],
  countries: [{ code: "GA", name: "Gabon", currency: "XAF", currencyDecimals: 0 }],
  profileComplete: false,
  avatarVersion: null,
};

function renderShell() {
  return render(
    <I18nProvider>
      <QueryProvider>
        <ToastProvider>
          <AppShell>
            <p>console body</p>
          </AppShell>
        </ToastProvider>
      </QueryProvider>
    </I18nProvider>,
  );
}

describe("profile completion gate", () => {
  it("blocks the console until the profile is complete", async () => {
    vi.stubGlobal("fetch", vi.fn<typeof fetch>(async () => Response.json(incomplete)));

    renderShell();

    expect(await screen.findByRole("heading", { name: "Complétez votre profil" })).toBeInTheDocument();
    expect(screen.queryByText("console body")).not.toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "Navigation principale" })).not.toBeInTheDocument();
  });

  it("requires a first and last name, keeps the phone optional, then lets the admin in", async () => {
    let complete = false;
    const fetchMock = vi.fn<typeof fetch>(async (input, init) => {
      if (String(input) === "/api/admin/me/profile" && init?.method === "PUT") {
        complete = true;
        return new Response(null, { status: 204 });
      }
      return Response.json(complete ? { ...incomplete, firstName: "Aline", lastName: "Obame", profileComplete: true } : incomplete);
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderShell();

    await user.click(await screen.findByRole("button", { name: "Continuer" }));
    expect(screen.getByText("Saisissez votre prénom.")).toBeInTheDocument();
    expect(screen.getByText("Saisissez votre nom.")).toBeInTheDocument();
    await user.type(screen.getByLabelText("Prénom"), "Aline");
    await user.type(screen.getByLabelText("Nom"), "Obame");
    await user.click(screen.getByRole("button", { name: "Continuer" }));

    await waitFor(() => {
      const put = fetchMock.mock.calls.find(([, init]) => init?.method === "PUT");
      expect(JSON.parse(String(put?.[1]?.body))).toEqual({ firstName: "Aline", lastName: "Obame", phone: "" });
    });
    expect(await screen.findByText("console body")).toBeInTheDocument();
  });
});
