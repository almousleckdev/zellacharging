"use client";

import { Fragment } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "./Icon";

export function Breadcrumbs({ trail }: { trail: string[] }) {
  const { t } = useI18n();

  return (
    <nav aria-label={t.shell.breadcrumb}>
      <ol className="flex flex-wrap items-center gap-1.5 text-sm text-subtle">
        {trail.map((label, index) => {
          const last = index === trail.length - 1;
          return (
            <Fragment key={label}>
              <li aria-current={last ? "page" : undefined} className={last ? "font-medium text-muted" : undefined}>
                {label}
              </li>
              {!last && (
                <li aria-hidden="true">
                  <Icon name="chevronRight" className="size-3.5" />
                </li>
              )}
            </Fragment>
          );
        })}
      </ol>
    </nav>
  );
}
