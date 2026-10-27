import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { renderWithI18n } from "@/test/renderWithI18n";
import { LanguageSwitch } from "@/shared/ui/LanguageSwitch";
import { LoginForm } from "./LoginForm";

vi.mock("next/navigation", () => ({ useRouter: () => ({ replace: vi.fn(), refresh: vi.fn() }) }));

describe("language switch", () => {
  it("defaults to French and switches the whole form to English", async () => {
    const user = userEvent.setup();
    renderWithI18n(
      <>
        <LanguageSwitch />
        <LoginForm />
      </>,
    );

    expect(screen.getByRole("heading", { name: "Connexion à la console" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "FR" })).toHaveAttribute("aria-pressed", "true");

    await user.click(screen.getByRole("button", { name: "EN" }));

    expect(screen.getByRole("heading", { name: "Sign in to the console" })).toBeInTheDocument();
    expect(screen.getByLabelText("Email address")).toBeInTheDocument();
    expect(document.documentElement.lang).toBe("en");
  });
});
