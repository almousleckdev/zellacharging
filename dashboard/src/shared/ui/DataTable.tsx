import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

export type Column<Row> = {
  key: string;
  header: string;
  align?: "left" | "right";
  cell: (row: Row) => ReactNode;
};

type DataTableProps<Row> = {
  caption: string;
  columns: Column<Row>[];
  rows: Row[];
  rowKey: (row: Row) => string;
};

export function DataTable<Row>({ caption, columns, rows, rowKey }: DataTableProps<Row>) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[40rem] border-collapse text-sm">
        <caption className="sr-only">{caption}</caption>
        <thead>
          <tr className="border-b border-border bg-surface text-left">
            {columns.map((column) => (
              <th
                key={column.key}
                scope="col"
                className={cn("px-5 py-3 text-xs font-semibold uppercase tracking-wide text-subtle", column.align === "right" && "text-right")}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)} className="border-b border-border last:border-b-0">
              {columns.map((column) => (
                <td key={column.key} className={cn("px-5 py-3.5 text-foreground", column.align === "right" && "text-right tabular-nums")}>
                  {column.cell(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
