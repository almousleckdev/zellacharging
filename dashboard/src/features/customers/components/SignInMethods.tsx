"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import type { CustomerDetail } from "../schemas";

type Methods = NonNullable<CustomerDetail["signInMethods"]>;

export function SignInMethods({ methods }: { methods: Methods }) {
  const { t } = useI18n();
  return (
    <span className="flex flex-col items-start gap-1">
      {methods.map((method) => (
        <Badge key={method} tone="neutral">
          {t.customers.detail.signInMethods[method]}
        </Badge>
      ))}
    </span>
  );
}
