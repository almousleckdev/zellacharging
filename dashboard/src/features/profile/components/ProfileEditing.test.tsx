import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { ProfilePage } from "./ProfilePage";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/profile",
  useSearchParams: () => new URLSearchParams(),
}));

function serve(handler: (url: string, init?: RequestInit) => Response) {
  const fetchMock = vi.fn<typeof fetch>(async (input, init) => handler(String(input), init));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("profile editing", () => {
  it("shows the saved details and the Mali dial code for a Mali number", () => {
    renderConsole(<ProfilePage />, { ...profileOf(), phone: "+22370123456" });

    expect(screen.getByLabelText("Prénom")).toHaveValue("Root");
    expect(screen.getByLabelText("Indicatif")).toHaveValue("+223");
    expect(screen.getByLabelText("Téléphone")).toHaveValue("70123456");
  });

  it("saves the details with the phone built from the dial code", async () => {
    const fetchMock = serve(() => new Response(null, { status: 204 }));
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);

    await user.clear(screen.getByLabelText("Prénom"));
    await user.type(screen.getByLabelText("Prénom"), "Aline");
    await user.selectOptions(screen.getByLabelText("Indicatif"), "+223");
    await user.type(screen.getByLabelText("Téléphone"), "70 12-34 56");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => {
      const put = fetchMock.mock.calls.find(([url]) => String(url) === "/api/admin/me/profile");
      expect(JSON.parse(String(put?.[1]?.body))).toEqual({ firstName: "Aline", lastName: "Admin", phone: "+22370123456" });
    });
    expect(await screen.findByText("Profil mis à jour.")).toBeInTheDocument();
  });

  it("rejects a phone number with too few digits before calling the server", async () => {
    const fetchMock = serve(() => new Response(null, { status: 204 }));
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);

    await user.type(screen.getByLabelText("Téléphone"), "123");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    expect(screen.getByText("Saisissez un numéro valide (chiffres uniquement).")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("shows the server's message when the number is refused", async () => {
    serve(() => Response.json({ code: "INVALID_PHONE" }, { status: 400 }));
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);

    await user.type(screen.getByLabelText("Téléphone"), "123456");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Ce numéro n'est pas valide pour le Gabon ou le Mali.");
  });

  it("uploads a picture with its own content type", async () => {
    const fetchMock = serve(() => new Response(null, { status: 204 }));
    const user = userEvent.setup();
    renderConsole(<ProfilePage />);
    const file = new File([new Uint8Array([1, 2, 3])], "me.png", { type: "image/png" });

    await user.upload(screen.getByLabelText("Changer la photo", { selector: "input" }), file);

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith("/api/admin/me/avatar", expect.objectContaining({ method: "PUT", body: file })));
    expect((fetchMock.mock.calls[0][1]?.headers as Record<string, string>)["Content-Type"]).toBe("image/png");
    expect(await screen.findByText("Photo mise à jour.")).toBeInTheDocument();
  });

  it("refuses an unsupported or oversized picture without uploading", async () => {
    const fetchMock = serve(() => new Response(null, { status: 204 }));
    const user = userEvent.setup({ applyAccept: false });
    renderConsole(<ProfilePage />);
    const input = screen.getByLabelText("Changer la photo", { selector: "input" });

    await user.upload(input, new File(["gif"], "a.gif", { type: "image/gif" }));
    await user.upload(input, new File([new Uint8Array(1_100_000)], "big.png", { type: "image/png" }));

    expect(await screen.findAllByRole("alert")).not.toHaveLength(0);
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("offers to remove the picture only when there is one", async () => {
    const fetchMock = serve(() => new Response(null, { status: 204 }));
    const user = userEvent.setup();
    renderConsole(<ProfilePage />, { ...profileOf(), avatarVersion: 123 });

    await user.click(screen.getByRole("button", { name: "Supprimer la photo" }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith("/api/admin/me/avatar", expect.objectContaining({ method: "DELETE" })));
  });
});
