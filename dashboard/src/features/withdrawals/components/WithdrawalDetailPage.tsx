"use client";

import { useParams } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { CellLink } from "@/shared/ui/CellLink";
import { DetailShell } from "@/shared/ui/DetailShell";
import { OperatorBadge } from "@/shared/ui/OperatorBadge";
import { RelatedLinks } from "@/shared/ui/RelatedLinks";
import { FactList } from "@/shared/ui/FactList";
import { useWithdrawalDetail } from "../api/useWithdrawalDetail";
import { withdrawalAmount } from "../format";
import { DecisionButtons } from "./DecisionButtons";
import { WithdrawalStateBadge } from "./WithdrawalStateBadge";

export function WithdrawalDetailPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const params = useParams<{ country: string; id: string }>();
  const query = useWithdrawalDetail(params.country, params.id);
  const labels = t.withdrawals.detail;
  const data = query.data;
  const payment = data?.payment;
  const title = payment ? `${t.withdrawals.title} · ${payment.reference}` : t.withdrawals.title;

  return (
    <DetailShell
      backHref="/withdrawals"
      backLabel={labels.back}
      title={title}
      icon="wallet"
      query={query}
      notFound={labels.notFound}
      loadError={labels.loadError}
      badge={payment && <WithdrawalStateBadge state={payment.state} />}
      actions={payment && <DecisionButtons withdrawal={payment} />}
    >
      {data && payment && (
        <div className="grid items-start gap-6 lg:grid-cols-2">
          <Card title={labels.operation}>
            <FactList
              facts={[
                { label: labels.amount, value: <span className="figure font-semibold">{withdrawalAmount(payment, format)}</span> },
                { label: labels.reference, value: <span className="font-mono text-xs">{payment.reference}</span> },
                { label: labels.provider, value: <span className="font-mono text-xs">{data.providerRef ?? t.common.none}</span> },
                { label: labels.failure, value: payment.failureCode ?? t.common.none },
              ]}
            />
          </Card>
          <Card title={labels.customer}>
            <FactList
              facts={[
                {
                  label: labels.name,
                  value: (
                    <CellLink href={scopedHref(`/customers/${payment.country}/${payment.customerId}`)} className="font-medium">
                      {payment.customerName}
                    </CellLink>
                  ),
                },
                { label: labels.operator, value: <OperatorBadge operator={payment.operator} showName /> },
                { label: labels.number, value: payment.msisdn },
              ]}
            />
          </Card>
          <RelatedLinks
            customerHref={`/customers/${payment.country}/${payment.customerId}`}
            links={[
              { label: labels.relatedCustomer, query: "" },
              { label: labels.relatedWallet, query: "?tab=wallet" },
            ]}
          />
          <Card title={labels.timeline}>
            <FactList
              facts={[
                { label: labels.created, value: format.dateTime(payment.createdAt) },
                { label: labels.updated, value: format.dateTime(data.updatedAt) },
              ]}
            />
          </Card>
        </div>
      )}
    </DetailShell>
  );
}
