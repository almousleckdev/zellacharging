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
import { DecisionButtons } from "@/features/withdrawals/components/DecisionButtons";
import { usePaymentDetail } from "../api/usePaymentDetail";
import { signedAmount } from "../format";
import { PaymentStateBadge } from "./PaymentStateBadge";

export function PaymentDetailPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const params = useParams<{ country: string; kind: string; id: string }>();
  const query = usePaymentDetail(params.country, params.kind, params.id);
  const labels = t.payments.detail;
  const kinds: Record<string, string> = t.payments.kinds;
  const data = query.data;
  const payment = data?.payment;
  const title = payment ? `${kinds[payment.kind] ?? payment.kind} · ${payment.reference}` : t.payments.title;

  return (
    <DetailShell
      backHref="/payments"
      backLabel={labels.back}
      title={title}
      icon="wallet"
      query={query}
      notFound={labels.notFound}
      loadError={labels.loadError}
      badge={payment && <PaymentStateBadge state={payment.state} />}
      actions={payment?.kind === "WITHDRAWAL" && <DecisionButtons withdrawal={payment} />}
    >
      {data && payment && (
        <div className="grid items-start gap-6 lg:grid-cols-2">
          <Card title={labels.operation}>
            <FactList
              facts={[
                { label: labels.type, value: kinds[payment.kind] ?? payment.kind },
                { label: labels.amount, value: <span className="figure font-semibold">{signedAmount(payment, format)}</span> },
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
              { label: labels.relatedPayments, query: "?tab=payments" },
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
