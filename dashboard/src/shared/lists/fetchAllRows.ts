import type { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";
import { EXPORT_MAX_ROWS, EXPORT_PAGE_SIZE, filterSearch } from "./filters";
import type { ListFilters } from "./filters";

type Listing<Row> = { total: number; items: Row[] };

export type AllRows<Row> = { rows: Row[]; truncated: boolean };

export async function fetchAllRows<Row, Out extends Listing<Row>>(
  resource: string,
  filters: ListFilters,
  schema: z.ZodType<Out>,
): Promise<AllRows<Row>> {
  const rows: Row[] = [];
  let total = 0;
  for (let page = 0; page * EXPORT_PAGE_SIZE < EXPORT_MAX_ROWS; page += 1) {
    const query = filterSearch(filters, page, EXPORT_PAGE_SIZE);
    const found = await adminFetch(`${resource}?${query.toString()}`, schema);
    total = found.total;
    rows.push(...found.items);
    if (found.items.length < EXPORT_PAGE_SIZE || rows.length >= total) {
      break;
    }
  }
  return { rows, truncated: total > rows.length };
}
