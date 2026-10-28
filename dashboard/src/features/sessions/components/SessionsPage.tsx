"use client";

import { useSession } from "@/features/session/SessionProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { ListPanel } from "@/shared/ui/ListPanel";
import { PageHeader } from "@/shared/ui/PageHeader";
import { PAGE_SIZE, useSessions } from "../api/useSessions";
import { SessionsTable } from "./SessionsTable";
import { SessionsToolbar } from "./SessionsToolbar";

export function SessionsPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country } = useSession();
  const { params, update } = useUrlParams();
  const text = params.get("q") ?? "";
  const state = params.get("state") ?? "";
  const page = Math.max(0, Number(params.get("page")) || 0);
  const sessions = useSessions({ country, text, state, page });
  const data = sessions.data;

  return (
    <div className="flex flex-col">
      <PageHeader
        title={t.sessions.title}
        subtitle={t.sessions.subtitle}
        trail={[t.shell.breadcrumbRoot, t.sessions.title]}
        actions={data && <p className="text-sm text-muted">{interpolate(t.sessions.results, { count: format.number(data.total) })}</p>}
      />
      <ListPanel
        toolbar={<SessionsToolbar text={text} state={state} onText={(value) => update({ q: value })} onState={(value) => update({ state: value })} />}
        pending={sessions.isPending}
        failed={sessions.isError}
        onRetry={() => void sessions.refetch()}
        loadError={t.sessions.loadError}
        empty={{ icon: "bolt", title: t.sessions.emptyTitle, text: t.sessions.emptyText }}
        isEmpty={data?.items.length === 0}
        page={page}
        pageCount={data ? Math.ceil(data.total / PAGE_SIZE) : 0}
        onPage={(next) => update({ page: next === 0 ? null : String(next) }, false)}
      >
        {data && data.items.length > 0 && <SessionsTable items={data.items} />}
      </ListPanel>
    </div>
  );
}
