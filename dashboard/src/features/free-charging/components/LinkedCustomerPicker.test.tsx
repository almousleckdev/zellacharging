import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { CustomerItem } from "@/features/customers/schemas";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import { LinkedCustomerPicker } from "./LinkedCustomerPicker";

const navigation = vi.hoisted(() => ({ search: new URLSearchParams() }));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn(), push: vi.fn() }),
  usePathname: () => "/free-charging",
  useSearchParams: () => navigation.search,
}));

function customer(id: string, firstName: string, lastName: string, phone: string | null): CustomerItem {
  return { id, country: "GA", firstName, lastName, email: null, phone, status: "ACTIVE", createdAt: "2026-01-01T00:00:00Z", ndzelaDriver: true };
}

const JEAN = customer("c1", "Jean", "Mba", "+24177000001");
const PAUL = customer("c2", "Paul", "Ndong", null);

function serve(answer: (query: URLSearchParams) => CustomerItem[]) {
  const fetchMock = vi.fn<typeof fetch>(async (input) => {
    const query = new URL(String(input), "http://localhost").searchParams;
    const items = answer(query);
    return Response.json({ total: items.length, page: 0, size: 8, items });
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

function Harness({ onChange }: { onChange: (customer: CustomerItem) => void }) {
  const [selected, setSelected] = useState<CustomerItem | null>(null);
  return (
    <LinkedCustomerPicker
      selected={selected}
      onSelect={(next) => {
        setSelected(next);
        onChange(next);
      }}
    />
  );
}

describe("LinkedCustomerPicker", () => {
  beforeEach(() => {
    navigation.search = new URLSearchParams();
  });

  it("lists the linked customers with first name, last name and phone, asking only for NDZela drivers", async () => {
    const fetchMock = serve(() => [JEAN, PAUL]);

    renderConsole(<Harness onChange={vi.fn()} />, profileOf([gabon]));

    const jean = await screen.findByRole("option", { name: /Jean Mba/ });
    expect(jean).toHaveTextContent("+24177000001");
    expect(screen.getByRole("option", { name: /Paul Ndong/ })).toHaveTextContent("Sans téléphone");
    const query = new URL(String(fetchMock.mock.calls[0][0]), "http://localhost").searchParams;
    expect(query.get("ndzelaDriver")).toBe("true");
    expect(query.get("country")).toBe("GA");
  });

  it("searches by the typed text", async () => {
    const fetchMock = serve((query) => ((query.get("q") ?? "").toLowerCase() === "paul" ? [PAUL] : [JEAN, PAUL]));
    const userEvents = userEvent.setup();
    renderConsole(<Harness onChange={vi.fn()} />);
    await screen.findByRole("option", { name: /Jean Mba/ });

    await userEvents.type(screen.getByRole("searchbox", { name: "Rechercher un client" }), "paul");

    await waitFor(() => expect(screen.queryByRole("option", { name: /Jean Mba/ })).not.toBeInTheDocument());
    expect(screen.getByRole("option", { name: /Paul Ndong/ })).toBeInTheDocument();
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes("q=paul"))).toBe(true);
  });

  it("keeps the chosen customer visible when the search text changes", async () => {
    serve((query) => ((query.get("q") ?? "") === "paul" ? [PAUL] : [JEAN, PAUL]));
    const onChange = vi.fn();
    const userEvents = userEvent.setup();
    renderConsole(<Harness onChange={onChange} />);

    await userEvents.click(await screen.findByRole("option", { name: /Jean Mba/ }));
    expect(onChange).toHaveBeenCalledWith(JEAN);
    expect(screen.getByText("Client choisi")).toBeInTheDocument();

    await userEvents.type(screen.getByRole("searchbox", { name: "Rechercher un client" }), "paul");
    await waitFor(() => expect(screen.queryByRole("option", { name: /Jean Mba/ })).not.toBeInTheDocument());

    const chosen = screen.getByText("Client choisi").parentElement as HTMLElement;
    expect(within(chosen).getByText("Jean Mba")).toBeInTheDocument();
    expect(within(chosen).getByText("+24177000001")).toBeInTheDocument();
  });

  it("says so when nobody matches", async () => {
    serve(() => []);

    renderConsole(<Harness onChange={vi.fn()} />);

    expect(await screen.findByText("Aucun chauffeur NDZela lié trouvé.")).toBeInTheDocument();
  });

  it("shows a readable error when the customers cannot be loaded", async () => {
    vi.stubGlobal("fetch", vi.fn<typeof fetch>(async () => Response.json({ code: "UNKNOWN" }, { status: 400 })));

    renderConsole(<Harness onChange={vi.fn()} />);

    expect(await screen.findByText("Impossible de charger les clients.")).toBeInTheDocument();
  });

  it("explains that the Customers permission is required when the list is refused", async () => {
    vi.stubGlobal("fetch", vi.fn<typeof fetch>(async () => Response.json({ code: "PERMISSION_DENIED" }, { status: 403 })));

    renderConsole(<Harness onChange={vi.fn()} />);

    expect(await screen.findByText("La permission Clients est nécessaire pour choisir un client.")).toBeInTheDocument();
    expect(screen.queryByText("Impossible de charger les clients.")).not.toBeInTheDocument();
  });
});
