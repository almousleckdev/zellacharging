"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { Button } from "@/shared/ui/Button";
import { EmptyState } from "@/shared/ui/EmptyState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { PillTabs } from "@/shared/ui/PillTabs";
import type { FreeChargingDialog } from "../dialog";
import { AgreementsTab } from "./AgreementsTab";
import { UsageTab } from "./UsageTab";
import { WindowCard } from "./WindowCard";

type Tab = "drivers" | "history";

export function FreeChargingPage() {
  const { t } = useI18n();
  const { profile } = useSession();
  const { params, update } = useUrlParams();
  const [dialog, setDialog] = useState<FreeChargingDialog>(null);
  const tab: Tab = params.get("tab") === "history" ? "history" : "drivers";
  const labels = t.freeCharging;

  if (!profile.permissions.includes("FREE_CHARGING_READ")) {
    return <EmptyState icon="shield" title={errorMessage(t, "PERMISSION_DENIED")} text="" />;
  }

  const canManage = profile.permissions.includes("FREE_CHARGING_MANAGE");

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={labels.title}
        subtitle={labels.subtitle}
        actions={
          <>
            <PillTabs
              label={labels.tabs.label}
              options={[
                { value: "drivers", label: labels.tabs.drivers },
                { value: "history", label: labels.tabs.history },
              ]}
              value={tab}
              onChange={(value) => update({ tab: value === "drivers" ? null : value })}
            />
            {canManage && <Button onClick={() => setDialog({ kind: "form", agreement: null })}>{labels.add}</Button>}
          </>
        }
      />
      <WindowCard />
      {tab === "drivers" ? <AgreementsTab dialog={dialog} onDialog={setDialog} /> : <UsageTab />}
    </div>
  );
}
