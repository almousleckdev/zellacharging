"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { PAGE_SIZES } from "@/shared/lists/filters";
import { Button } from "./Button";

type PaginationProps = {
  page: number;
  size: number;
  total: number;
  onPage: (page: number) => void;
  onSize?: (size: number) => void;
};

export function Pagination({ page, size, total, onPage, onSize }: PaginationProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.common.pagination;
  const pageCount = Math.ceil(total / size);

  if (total === 0) {
    return null;
  }

  const from = page * size + 1;
  const to = Math.min(total, (page + 1) * size);

  return (
    <nav aria-label={labels.label} className="flex flex-wrap items-center justify-between gap-3 border-t border-border px-4 py-3 sm:px-5">
      <div className="flex flex-wrap items-center gap-4 text-sm text-muted">
        <p>{interpolate(labels.summary, { from: format.number(from), to: format.number(to), total: format.number(total) })}</p>
        {onSize && (
        <label className="flex items-center gap-2">
          <span>{labels.perPage}</span>
          <select
            value={size}
            onChange={(event) => onSize(Number(event.target.value))}
            className="h-8 rounded-control border border-transparent bg-surface-strong px-2 text-sm text-foreground"
          >
            {PAGE_SIZES.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
        )}
      </div>
      <div className="flex items-center gap-3">
        <p className="text-sm text-muted">{interpolate(labels.page, { page: page + 1, pages: pageCount })}</p>
        <div className="flex gap-2">
          <Button variant="secondary" className="h-9 px-4" disabled={page === 0} onClick={() => onPage(page - 1)}>
            {labels.previous}
          </Button>
          <Button variant="secondary" className="h-9 px-4" disabled={page + 1 >= pageCount} onClick={() => onPage(page + 1)}>
            {labels.next}
          </Button>
        </div>
      </div>
    </nav>
  );
}
