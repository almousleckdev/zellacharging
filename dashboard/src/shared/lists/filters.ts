import { rangeToInstants } from "@/shared/lib/dateRange";

export const PAGE_SIZES = [10, 20, 50, 100] as const;
export const DEFAULT_PAGE_SIZE = 10;
export const EXPORT_PAGE_SIZE = 100;
export const EXPORT_MAX_ROWS = 10000;

export type ListFilters = {
  country: string;
  text: string;
  from: string;
  to: string;
  page: number;
  size: number;
  sort: string;
  extra: Record<string, string>;
};

export function filterSearch(filters: ListFilters, page = filters.page, size = filters.size): URLSearchParams {
  const query = new URLSearchParams({ country: filters.country, page: String(page), size: String(size) });
  if (filters.text) {
    query.set("q", filters.text);
  }
  if (filters.sort) {
    query.set("sort", filters.sort);
  }
  const instants = rangeToInstants(filters);
  if (instants.from) {
    query.set("from", instants.from);
  }
  if (instants.to) {
    query.set("to", instants.to);
  }
  for (const [key, value] of Object.entries(filters.extra)) {
    if (value) {
      query.set(key, value);
    }
  }
  return query;
}

export function filterKey(filters: ListFilters): string[] {
  return [
    filters.text,
    filters.from,
    filters.to,
    String(filters.page),
    String(filters.size),
    filters.sort,
    ...Object.entries(filters.extra).map(([key, value]) => `${key}=${value}`),
  ];
}
