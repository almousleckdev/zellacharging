import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { gabon, mali, profileOf, renderConsole } from "@/test/renderConsole";
import { CustomersPage } from "./CustomersPage";

const navigation = vi.hoisted(() => ({ replace: vi.fn(), search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: navigation.replace }),
  usePathname: () => "/customers",
  useSearchParams: () => navigation.search,
}));

function item(id: string, first: string, country: string, status = "ACTIVE") {
  return {
    id,
    country,
    firstName: first,
    lastName: "Obame",
    email: `${first.toLowerCase()}@example.com`,
    phone: "+24177123456",
    status,
    createdAt: "2026-10-01T10:00:00Z",
  };
}

function serve(body: unknown, status = 200) {
  const fetchMock = vi.fn<typeof fetch>(async () => Response.json(body, { status }));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

describe("CustomersPage", () => {
  beforeEach(() => {
    navigation.replace.mockClear();
    navigation.search = new URLSearchParams();
  });

  it("shows a shimmer table while customers load", () => {
    vi.stubGlobal("fetch", vi.fn(() => new Promise(() => undefined)));

    renderConsole(<CustomersPage />);

    expect(screen.getByRole("status")).toHaveAttribute("aria-busy", "true");
  });

  it("lists customers with their status and a link to the right country record", async () => {
    navigation.search = new URLSearchParams("country=GA");
    const fetchMock = serve({ total: 2, page: 0, size: 20, items: [item("c1", "Aline", "GA"), item("c2", "Brice", "GA", "BLOCKED")] });

    renderConsole(<CustomersPage />);

    const link = await screen.findByRole("link", { name: /Aline Obame/ });
    expect(link).toHaveAttribute("href", "/customers/GA/c1?country=GA");
    expect(within(await screen.findByRole("table")).getByText("Bloqué")).toBeInTheDocument();
    expect(screen.getByText("2 clients")).toBeInTheDocument();
    expect(screen.queryByRole("columnheader", { name: "Pays" })).not.toBeInTheDocument();
    expect(String(fetchMock.mock.calls[0][0])).toContain("/api/admin/customers?country=GA");
  });

  it("adds a country column when several countries are shown", async () => {
    serve({ total: 2, page: 0, size: 20, items: [item("c1", "Aline", "GA"), item("c3", "Amadou", "ML")] });

    renderConsole(<CustomersPage />, profileOf([gabon, mali]));

    const table = await screen.findByRole("table");
    expect(within(table).getByRole("columnheader", { name: "Pays" })).toBeInTheDocument();
    expect(within(table).getByText("Mali")).toBeInTheDocument();
  });

  it("puts the search text and the status filter in the URL", async () => {
    serve({ total: 0, page: 0, size: 20, items: [] });
    const user = userEvent.setup();
    renderConsole(<CustomersPage />);

    await user.type(await screen.findByLabelText("Rechercher un client"), "aline");
    await waitFor(() => expect(navigation.replace).toHaveBeenCalledWith("/customers?q=aline"));
    await user.selectOptions(screen.getByLabelText("Statut"), "BLOCKED");
    expect(navigation.replace).toHaveBeenCalledWith("/customers?status=BLOCKED");
  });

  it("sends the filters to the server and pages through results", async () => {
    navigation.search = new URLSearchParams("country=GA&q=aline&status=ACTIVE&page=1");
    const fetchMock = serve({ total: 45, page: 1, size: 20, items: [item("c1", "Aline", "GA")] });
    const user = userEvent.setup();
    renderConsole(<CustomersPage />, profileOf([gabon, mali]));

    expect(await screen.findByText("Page 2 sur 3")).toBeInTheDocument();
    const url = String(fetchMock.mock.calls[0][0]);
    expect(url).toContain("q=aline");
    expect(url).toContain("status=ACTIVE");
    expect(url).toContain("page=1");
    await user.click(screen.getByRole("button", { name: "Suivant" }));
    expect(navigation.replace).toHaveBeenCalledWith("/customers?country=GA&q=aline&status=ACTIVE&page=2");
  });

  it("explains an empty result", async () => {
    serve({ total: 0, page: 0, size: 20, items: [] });

    renderConsole(<CustomersPage />);

    expect(await screen.findByText("Aucun client trouvé")).toBeInTheDocument();
  });

  it("offers a retry when loading fails", async () => {
    const fetchMock = serve({ code: "FORBIDDEN" }, 403);
    const user = userEvent.setup();

    renderConsole(<CustomersPage />);

    expect(await screen.findByRole("alert")).toHaveTextContent("Impossible de charger les clients.");
    await user.click(screen.getByRole("button", { name: "Réessayer" }));
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(2));
  });
});
