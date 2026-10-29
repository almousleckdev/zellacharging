"use client";

import { useMemo } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from "./filters";
import type { ListFilters } from "./filters";

export function useListFilters(extraKeys: readonly string[] = [], fixed: Record<string, string> = {}) {
  const { country } = useSession();
  const { params, update } = useUrlParams();
  const requestedSize = Number(params.get("size"));
  const size = (PAGE_SIZES as readonly number[]).includes(requestedSize) ? requestedSize : DEFAULT_PAGE_SIZE;
  const extraSignature = extraKeys.map((key) => params.get(key) ?? "").join("|");
  const fixedSignature = JSON.stringify(fixed);

  const filters = useMemo<ListFilters>(() => {
    const extra: Record<string, string> = { ...(JSON.parse(fixedSignature) as Record<string, string>) };
    extraKeys.forEach((key, index) => {
      extra[key] = extraSignature.split("|")[index] ?? "";
    });
    return {
      country,
      text: params.get("q") ?? "",
      from: params.get("from") ?? "",
      to: params.get("to") ?? "",
      page: Math.max(0, Number(params.get("page")) || 0),
      size,
      sort: params.get("sort") ?? "",
      extra,
    };
  }, [country, params, size, extraKeys, extraSignature, fixedSignature]);

  return {
    filters,
    setText: (value: string) => update({ q: value }),
    setExtra: (key: string, value: string) => update({ [key]: value }),
    setRange: (from: string, to: string) => update({ from, to }),
    setPage: (page: number) => update({ page: page === 0 ? null : String(page) }, false),
    setSort: (value: string) => update({ sort: value }),
    setSize: (value: number) => update({ size: value === DEFAULT_PAGE_SIZE ? null : String(value) }),
  };
}
