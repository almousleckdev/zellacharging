import { render } from "@testing-library/react";
import type { ReactElement } from "react";
import { SessionProvider } from "@/features/session/SessionProvider";
import type { AdminProfile } from "@/features/session/schemas";
import { QueryProvider } from "@/shared/api/QueryProvider";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
import { ToastProvider } from "@/shared/ui/ToastProvider";

export const gabon = { code: "GA", name: "Gabon", currency: "XAF", currencyDecimals: 0 };
export const mali = { code: "ML", name: "Mali", currency: "XOF", currencyDecimals: 0 };

export function profileOf(countries = [gabon, mali], role = "SUPER_ADMIN"): AdminProfile {
  return {
    id: "1",
    email: "root@zella.test",
    firstName: "Root",
    lastName: "Admin",
    phone: null,
    role,
    permissions: ["OVERVIEW_READ", "ADMINS_MANAGE"],
    countries,
    profileComplete: true,
    avatarVersion: null,
  };
}

export function renderConsole(ui: ReactElement, profile: AdminProfile = profileOf()) {
  return render(
    <I18nProvider>
      <QueryProvider>
        <ToastProvider>
          <SessionProvider profile={profile}>{ui}</SessionProvider>
        </ToastProvider>
      </QueryProvider>
    </I18nProvider>,
  );
}
