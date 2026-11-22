import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { profileOf, renderConsole } from "@/test/renderConsole";
import { WindowCard } from "./WindowCard";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/free-charging",
  useSearchParams: () => new URLSearchParams(),
}));

const WHOLE_SUNDAY = { day: "SUNDAY", from: "00:00", until: "23:59", endOfDay: true };

const withPermissions = (...permissions: string[]) => ({ ...profileOf(), permissions });
const manager = () => withPermissions("FREE_CHARGING_READ", "FREE_CHARGING_MANAGE");
const reader = () => withPermissions("FREE_CHARGING_READ");

type Handler = (url: string, init?: RequestInit) => Response;

function serve(handler: Handler) {
  const fetchMock = vi.fn<typeof fetch>(async (input, init) => handler(String(input), init));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

const puts = (fetchMock: ReturnType<typeof serve>) => fetchMock.mock.calls.filter(([, init]) => init?.method === "PUT");

describe("WindowCard", () => {
  beforeEach(() => {
    vi.unstubAllGlobals();
  });

  it("tells the window in words", async () => {
    serve(() => Response.json(WHOLE_SUNDAY));

    renderConsole(<WindowCard />, reader());

    expect(await screen.findByText("Recharge offerte le dimanche, de 00h00 à 23h59 (heure du Gabon).")).toBeInTheDocument();
  });

  it("shows no edit button without the manage permission", async () => {
    serve(() => Response.json(WHOLE_SUNDAY));

    renderConsole(<WindowCard />, reader());

    await screen.findByText(/Recharge offerte/);
    expect(screen.queryByRole("button", { name: "Modifier le créneau" })).not.toBeInTheDocument();
  });

  it("says so when the window cannot be loaded", async () => {
    serve(() => Response.json({ code: "UNKNOWN" }, { status: 400 }));

    renderConsole(<WindowCard />, reader());

    expect(await screen.findByText("Impossible de charger le créneau gratuit.")).toBeInTheDocument();
  });

  it("saves a bounded window through the BFF with the end time", async () => {
    const fetchMock = serve((_url, init) => Response.json(init?.method === "PUT" ? { day: "SATURDAY", from: "09:00", until: "17:30", endOfDay: false } : WHOLE_SUNDAY));
    const user = userEvent.setup();
    renderConsole(<WindowCard />, manager());

    await user.click(await screen.findByRole("button", { name: "Modifier le créneau" }));
    await user.selectOptions(screen.getByLabelText("Jour"), "SATURDAY");
    const start = screen.getByLabelText("Début");
    await user.clear(start);
    await user.type(start, "09:00");
    await user.click(screen.getByRole("checkbox", { name: /fin de la journée/ }));
    await user.type(screen.getByLabelText("Fin"), "17:30");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => expect(puts(fetchMock)).toHaveLength(1));
    const [url, init] = puts(fetchMock)[0];
    expect(String(url)).toContain("free-charging/window");
    expect(JSON.parse(String(init?.body))).toEqual({ day: "SATURDAY", from: "09:00", until: "17:30", endOfDay: false });
    expect(await screen.findByText("Recharge offerte le samedi, de 09h00 à 17h30 (heure du Gabon).")).toBeInTheDocument();
  });

  it("sends no end time for the whole day", async () => {
    const fetchMock = serve(() => Response.json(WHOLE_SUNDAY));
    const user = userEvent.setup();
    renderConsole(<WindowCard />, manager());

    await user.click(await screen.findByRole("button", { name: "Modifier le créneau" }));
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    await waitFor(() => expect(puts(fetchMock)).toHaveLength(1));
    expect(JSON.parse(String(puts(fetchMock)[0][1]?.body))).toEqual({ day: "SUNDAY", from: "00:00", until: null, endOfDay: true });
  });

  it("refuses an end that is not after the start and sends nothing", async () => {
    const fetchMock = serve(() => Response.json(WHOLE_SUNDAY));
    const user = userEvent.setup();
    renderConsole(<WindowCard />, manager());

    await user.click(await screen.findByRole("button", { name: "Modifier le créneau" }));
    await user.click(screen.getByRole("checkbox", { name: /fin de la journée/ }));
    await user.type(screen.getByLabelText("Fin"), "00:00");
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    expect(await screen.findByText("La fin doit venir après le début.")).toBeInTheDocument();
    expect(puts(fetchMock)).toHaveLength(0);
  });

  it("shows the mapped message and keeps the dialog open when the server refuses", async () => {
    serve((_url, init) => (init?.method === "PUT" ? Response.json({ code: "FREE_WINDOW_INVALID" }, { status: 422 }) : Response.json(WHOLE_SUNDAY)));
    const user = userEvent.setup();
    renderConsole(<WindowCard />, manager());

    await user.click(await screen.findByRole("button", { name: "Modifier le créneau" }));
    await user.click(screen.getByRole("button", { name: "Enregistrer" }));

    expect(await screen.findByText("Le créneau n'est pas valide : la fin doit venir après le début.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Enregistrer" })).toBeInTheDocument();
  });
});
