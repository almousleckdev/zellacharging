import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { renderWithI18n } from "@/test/renderWithI18n";
import { ForgotPasswordForm } from "./ForgotPasswordForm";
import { ResetPasswordForm } from "./ResetPasswordForm";

const search = vi.hoisted(() => ({ token: "abc" as string | null }));

vi.mock("next/navigation", () => ({
  useSearchParams: () => ({ get: () => search.token }),
}));

function answer(status: number, body: unknown = null) {
  const fetchMock = vi.fn().mockResolvedValue(
    status === 204 ? new Response(null, { status }) : Response.json(body, { status }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("password recovery", () => {
  beforeEach(() => {
    search.token = "abc";
  });

  it("asks for a valid email before calling the server", async () => {
    const fetchMock = answer(204);
    const user = userEvent.setup();
    renderWithI18n(<ForgotPasswordForm />);

    await user.type(screen.getByLabelText("Adresse e-mail"), "nope");
    await user.click(screen.getByRole("button", { name: "Envoyer le lien" }));

    expect(screen.getByText("Saisissez une adresse e-mail valide.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("confirms the request without revealing whether the account exists", async () => {
    const fetchMock = answer(204);
    const user = userEvent.setup();
    renderWithI18n(<ForgotPasswordForm />);

    await user.type(screen.getByLabelText("Adresse e-mail"), "root@zella.test");
    await user.click(screen.getByRole("button", { name: "Envoyer le lien" }));

    expect(await screen.findByText("Vérifiez votre boîte mail")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith("/api/auth/password/forgot", expect.objectContaining({ method: "POST" }));
  });

  it("sends the token and the new password", async () => {
    const fetchMock = answer(204);
    const user = userEvent.setup();
    renderWithI18n(<ResetPasswordForm />);

    await user.type(screen.getByLabelText("Nouveau mot de passe"), "Str0ng-Pass!42");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "Str0ng-Pass!42");
    await user.click(screen.getByRole("button", { name: "Modifier le mot de passe" }));

    expect(await screen.findByText("Mot de passe modifié")).toBeInTheDocument();
    await waitFor(() =>
      expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ token: "abc", password: "Str0ng-Pass!42" }),
    );
  });

  it("refuses mismatching passwords locally", async () => {
    const fetchMock = answer(204);
    const user = userEvent.setup();
    renderWithI18n(<ResetPasswordForm />);

    await user.type(screen.getByLabelText("Nouveau mot de passe"), "Str0ng-Pass!42");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "Different-Pass!1");
    await user.click(screen.getByRole("button", { name: "Modifier le mot de passe" }));

    expect(screen.getByText("Les deux mots de passe ne correspondent pas.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("shows the invalid link page when the link is expired", async () => {
    answer(404, { code: "RESET_LINK_INVALID" });
    const user = userEvent.setup();
    renderWithI18n(<ResetPasswordForm />);

    await user.type(screen.getByLabelText("Nouveau mot de passe"), "Str0ng-Pass!42");
    await user.type(screen.getByLabelText("Confirmer le mot de passe"), "Str0ng-Pass!42");
    await user.click(screen.getByRole("button", { name: "Modifier le mot de passe" }));

    expect(await screen.findByText("Lien invalide")).toBeInTheDocument();
  });

  it("shows the invalid link page when there is no token", () => {
    search.token = null;
    renderWithI18n(<ResetPasswordForm />);

    expect(screen.getByText("Lien invalide")).toBeInTheDocument();
  });
});
