import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { renderConsole } from "@/test/renderConsole";
import { DataTable } from "./DataTable";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  usePathname: () => "/",
  useSearchParams: () => new URLSearchParams(),
}));

const columns = [
  { key: "name", header: "Name", sortKey: "name", cell: (row: { name: string }) => row.name },
  { key: "plain", header: "Plain", cell: () => "x" },
];

describe("DataTable sorting", () => {
  it("cycles a column from descending to ascending to the default order", async () => {
    const onChange = vi.fn();
    const user = userEvent.setup();
    const { rerender } = renderConsole(
      <DataTable caption="t" columns={columns} rows={[{ name: "A" }]} rowKey={(row) => row.name} sort={{ value: "", onChange }} />,
    );

    await user.click(screen.getByRole("button", { name: "Name" }));
    expect(onChange).toHaveBeenLastCalledWith("name:desc");

    rerender(<DataTable caption="t" columns={columns} rows={[{ name: "A" }]} rowKey={(row) => row.name} sort={{ value: "name:desc", onChange }} />);
    await user.click(screen.getByRole("button", { name: "Name" }));
    expect(onChange).toHaveBeenLastCalledWith("name:asc");

    rerender(<DataTable caption="t" columns={columns} rows={[{ name: "A" }]} rowKey={(row) => row.name} sort={{ value: "name:asc", onChange }} />);
    await user.click(screen.getByRole("button", { name: "Name" }));
    expect(onChange).toHaveBeenLastCalledWith("");
    expect(screen.queryByRole("button", { name: "Plain" })).not.toBeInTheDocument();
  });
});

describe("DataTable on a narrow screen", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("stacks each row as a card instead of overflowing a table", () => {
    vi.stubGlobal("matchMedia", () => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() }));

    renderConsole(<DataTable caption="people" columns={columns} rows={[{ name: "Aline" }]} rowKey={(row) => row.name} />);

    expect(screen.queryByRole("table")).not.toBeInTheDocument();
    expect(screen.getByRole("list", { name: "people" })).toHaveTextContent("Aline");
    expect(screen.getByRole("list", { name: "people" })).toHaveTextContent("Plain");
  });
});
