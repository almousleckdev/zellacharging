"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { PAGE_SIZES } from "@/shared/lists/filters";
import { Button } from "./Button";
import { FIELD } from "./fieldStyles";
import { Icon } from "./Icon";

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
    <nav aria-label={labels.label} className="flex flex-wrap items-center justify-between gap-3 border-t border-border px-4 py-3">
      <div className="flex flex-wrap items-center gap-4 text-xs text-muted">
        <p>{interpolate(labels.summary, { from: format.number(from), to: format.number(to), total: format.number(total) })}</p>
        {onSize && (
        <label className="flex items-center gap-2">
          <span>{labels.perPage}</span>
          <select
            value={size}
            onChange={(event) => onSize(Number(event.target.value))}
            className={`h-8 px-2 ${FIELD}`}
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
        <p className="text-xs tabular-nums text-muted">{interpolate(labels.page, { page: page + 1, pages: pageCount })}</p>
        <div className="flex gap-1">
          <Button variant="secondary" className="size-8 px-0" aria-label={labels.previous} disabled={page === 0} onClick={() => onPage(page - 1)}>
            <Icon name="chevronDown" className="size-4 rotate-90" />
          </Button>
          <Button variant="secondary" className="size-8 px-0" aria-label={labels.next} disabled={page + 1 >= pageCount} onClick={() => onPage(page + 1)}>
            <Icon name="chevronRight" className="size-4" />
          </Button>
        </div>
      </div>
    </nav>
  );
}
