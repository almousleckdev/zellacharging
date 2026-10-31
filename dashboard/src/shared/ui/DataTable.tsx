"use client";

import { useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

export type Column<Row> = {
  key: string;
  header: string;
  align?: "left" | "right";
  cell: (row: Row) => ReactNode;
  text?: (row: Row) => string | number;
  sortKey?: string;
};

export type TableSort = { value: string; onChange: (value: string) => void };

type DataTableProps<Row> = {
  caption: string;
  columns: Column<Row>[];
  rows: Row[];
  rowKey: (row: Row) => string;
  rowHref?: (row: Row) => string;
  sort?: TableSort;
};

function nextSort(current: string, column: string): string {
  if (current === `${column}:desc`) {
    return `${column}:asc`;
  }
  return current === `${column}:asc` ? "" : `${column}:desc`;
}

function SortHeader({ column, label, sort }: { column: string; label: string; sort: TableSort }) {
  const direction = sort.value.startsWith(`${column}:`) ? sort.value.split(":")[1] : null;

  return (
    <button
      type="button"
      onClick={() => sort.onChange(nextSort(sort.value, column))}
      aria-label={label}
      className="inline-flex items-center gap-1 hover:text-foreground"
    >
      {label}
      <span aria-hidden="true" className={direction ? "text-primary-text" : "text-border-strong"}>
        {direction === "asc" ? "↑" : direction === "desc" ? "↓" : "↕"}
      </span>
    </button>
  );
}

export function DataTable<Row>({ caption, columns, rows, rowKey, rowHref, sort }: DataTableProps<Row>) {
  const router = useRouter();

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
                className={cn("px-5 py-3 text-xs font-medium text-subtle", column.align === "right" && "text-right")}
              >
                {column.sortKey && sort ? <SortHeader column={column.sortKey} label={column.header} sort={sort} /> : column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr
              key={rowKey(row)}
              onClick={rowHref ? () => router.push(rowHref(row)) : undefined}
              className={cn(
                "border-b border-border transition-colors last:border-b-0 hover:bg-surface",
                rowHref && "cursor-pointer",
              )}
            >
              {columns.map((column) => (
                <td key={column.key} className={cn("px-5 py-3 text-foreground", column.align === "right" && "whitespace-nowrap text-right tabular-nums")}>
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
