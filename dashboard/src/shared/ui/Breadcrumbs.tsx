"use client";

import Link from "next/link";
import { Fragment } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "./Icon";

export type Crumb = string | { label: string; href: string };

export function Breadcrumbs({ trail }: { trail: Crumb[] }) {
  const { t } = useI18n();
  const { scopedHref } = useSession();

  return (
    <nav aria-label={t.shell.breadcrumb}>
      <ol className="flex flex-wrap items-center gap-1.5 text-sm text-subtle">
        {trail.map((crumb, index) => {
          const last = index === trail.length - 1;
          const label = typeof crumb === "string" ? crumb : crumb.label;
          return (
            <Fragment key={`${index}-${label}`}>
              <li aria-current={last ? "page" : undefined} className={last ? "font-medium text-muted" : undefined}>
                {typeof crumb === "string" || last ? label : <Link href={scopedHref(crumb.href)} className="hover:text-foreground">{label}</Link>}
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
