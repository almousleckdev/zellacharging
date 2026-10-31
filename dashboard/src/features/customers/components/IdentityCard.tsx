"use client";

import { customerAvatarUrl, initialsOf } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Avatar } from "@/shared/ui/Avatar";
import { Badge } from "@/shared/ui/Badge";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";
import type { CustomerDetail } from "../schemas";
import { CustomerStatusBadge } from "./CustomerStatusBadge";

function Verification({ value, verified }: { value: string | null; verified: boolean }) {
  const { t } = useI18n();
  const labels = t.customers.detail;
  if (!value) {
    return <span className="text-subtle">{labels.none}</span>;
  }
  return (
    <span className="flex items-center justify-end gap-2">
      {value}
      <Badge tone={verified ? "success" : "neutral"}>{verified ? labels.verified : labels.unverified}</Badge>
    </span>
  );
}

export function IdentityCard({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const labels = t.customers.detail;
  const country = profile.countries.find((item) => item.code === customer.country)?.name ?? customer.country;

  return (
    <Card title={labels.identity}>
      <div className="mb-4 flex items-center gap-4">
        <Avatar
          size={64}
          initials={initialsOf(customer.firstName, customer.lastName, customer.email ?? "")}
          src={customerAvatarUrl(customer.country, customer.id, customer.avatarVersion)}
        />
        <p className="text-lg font-semibold text-foreground">{`${customer.firstName} ${customer.lastName}`}</p>
      </div>
      <FactList
        facts={[
          { label: labels.email, value: <Verification value={customer.email} verified={customer.emailVerified} /> },
          { label: labels.phone, value: <Verification value={customer.phone} verified={customer.phoneVerified} /> },
          { label: labels.country, value: country },
          { label: labels.status, value: <CustomerStatusBadge status={customer.status} /> },
          { label: labels.joined, value: format.date(customer.createdAt) },
        ]}
      />
    </Card>
  );
}
