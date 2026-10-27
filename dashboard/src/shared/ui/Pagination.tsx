"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "./Button";

type PaginationProps = {
  page: number;
  pageCount: number;
  onChange: (page: number) => void;
};

export function Pagination({ page, pageCount, onChange }: PaginationProps) {
  const { t } = useI18n();
  const labels = t.common.pagination;

  if (pageCount <= 1) {
    return null;
  }

  return (
    <nav aria-label={labels.label} className="flex items-center justify-between gap-3 border-t border-border px-5 py-3">
      <p className="text-sm text-muted">{interpolate(labels.page, { page: page + 1, pages: pageCount })}</p>
      <div className="flex gap-2">
        <Button variant="secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
          {labels.previous}
        </Button>
        <Button variant="secondary" disabled={page + 1 >= pageCount} onClick={() => onChange(page + 1)}>
          {labels.next}
        </Button>
      </div>
    </nav>
  );
}
