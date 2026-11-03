"use client";

import { useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";
import { useMediaQuery } from "@/shared/lib/useMediaQuery";

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

const WIDE_COLUMNS = 5;

function CardRow<Row>({ row, columns }: { row: Row; columns: Column<Row>[] }) {
  const [lead, ...rest] = columns;
  const facts = rest.filter((column) => column.header !== "");
  const actions = rest.filter((column) => column.header === "");

  return (
    <div className="flex flex-col gap-3">
      <div className="min-w-0 text-sm font-medium text-foreground">{lead.cell(row)}</div>
      <dl className="grid gap-2 text-sm">
        {facts.map((column) => (
          <div key={column.key} className="flex items-start justify-between gap-4">
            <dt className="shrink-0 text-xs text-subtle">{column.header}</dt>
            <dd className="min-w-0 text-right text-foreground [overflow-wrap:anywhere]">{column.cell(row)}</dd>
          </div>
        ))}
      </dl>
      {actions.map((column) => (
        <div key={column.key}>{column.cell(row)}</div>
      ))}
    </div>
  );
}

export function DataTable<Row>({ caption, columns, rows, rowKey, rowHref, sort }: DataTableProps<Row>) {
  const router = useRouter();
  const wide = columns.length > WIDE_COLUMNS;
  const tabular = useMediaQuery(wide ? "(min-width: 1280px)" : "(min-width: 768px)", true);
  const open = rowHref ? (row: Row) => router.push(rowHref(row)) : undefined;

  if (!tabular) {
    return (
      <ul aria-label={caption} className="divide-y divide-border">
        {rows.map((row) => (
          <li
            key={rowKey(row)}
            onClick={open ? () => open(row) : undefined}
            className={cn("px-4 py-4 transition-colors sm:px-5", open && "cursor-pointer hover:bg-surface")}
          >
            <CardRow row={row} columns={columns} />
          </li>
        ))}
      </ul>
    );
  }

  return (
    <table className="w-full border-collapse text-sm">
      <caption className="sr-only">{caption}</caption>
      <thead>
        <tr className="border-b border-border bg-surface text-left">
          {columns.map((column) => (
            <th
              key={column.key}
              scope="col"
              className={cn("whitespace-nowrap px-4 py-3 text-xs font-medium text-subtle first:pl-5 last:pr-5", column.align === "right" && "text-right")}
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
            onClick={open ? () => open(row) : undefined}
            className={cn("border-b border-border transition-colors last:border-b-0 hover:bg-surface", open && "cursor-pointer")}
          >
            {columns.map((column) => (
              <td
                key={column.key}
                className={cn(
                  "px-4 py-3.5 align-middle text-foreground first:pl-5 last:pr-5 [overflow-wrap:anywhere]",
                  column.align === "right" && "whitespace-nowrap text-right tabular-nums [overflow-wrap:normal]",
                )}
              >
                {column.cell(row)}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
