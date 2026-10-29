"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "./Card";
import { Icon } from "./Icon";

type RelatedLinksProps = {
  customerHref: string;
  links: { label: string; query: string }[];
};

export function RelatedLinks({ customerHref, links }: RelatedLinksProps) {
  const { t } = useI18n();
  const { scopedHref } = useSession();

  return (
    <Card title={t.common.related}>
      <ul className="flex flex-col divide-y divide-border">
        {links.map((link) => (
          <li key={link.label}>
            <Link
              href={scopedHref(`${customerHref}${link.query}`)}
              className="flex items-center justify-between py-2.5 text-sm font-medium text-foreground hover:text-primary-text"
            >
              {link.label}
              <Icon name="chevronRight" className="size-4 text-subtle" />
            </Link>
          </li>
        ))}
      </ul>
    </Card>
  );
}
