"use client";

import { useSession } from "@/features/session/SessionProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { ListPanel } from "@/shared/ui/ListPanel";
import { PageHeader } from "@/shared/ui/PageHeader";
import { PAGE_SIZE, usePayments } from "../api/usePayments";
import { PaymentsTable } from "./PaymentsTable";
import { PaymentsToolbar } from "./PaymentsToolbar";

export function PaymentsPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country } = useSession();
  const { params, update } = useUrlParams();
  const text = params.get("q") ?? "";
  const kind = params.get("kind") ?? "";
  const state = params.get("state") ?? "";
  const page = Math.max(0, Number(params.get("page")) || 0);
  const payments = usePayments({ country, text, kind, state, page });
  const data = payments.data;

  return (
    <div className="flex flex-col">
      <PageHeader
        title={t.payments.title}
        subtitle={t.payments.subtitle}
        trail={[t.shell.breadcrumbRoot, t.payments.title]}
        actions={data && <p className="text-sm text-muted">{interpolate(t.payments.results, { count: format.number(data.total) })}</p>}
      />
      <ListPanel
        toolbar={
          <PaymentsToolbar
            text={text}
            kind={kind}
            state={state}
            onText={(value) => update({ q: value })}
            onKind={(value) => update({ kind: value })}
            onState={(value) => update({ state: value })}
          />
        }
        pending={payments.isPending}
        failed={payments.isError}
        onRetry={() => void payments.refetch()}
        loadError={t.payments.loadError}
        empty={{ icon: "wallet", title: t.payments.emptyTitle, text: t.payments.emptyText }}
        isEmpty={data?.items.length === 0}
        page={page}
        pageCount={data ? Math.ceil(data.total / PAGE_SIZE) : 0}
        onPage={(next) => update({ page: next === 0 ? null : String(next) }, false)}
      >
        {data && data.items.length > 0 && <PaymentsTable items={data.items} />}
      </ListPanel>
    </div>
  );
}
