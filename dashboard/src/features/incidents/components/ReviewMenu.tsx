"use client";

import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ActionMenu } from "@/shared/ui/ActionMenu";
import type { IncidentItem } from "../schemas";
import { ReviewDialog } from "./ReviewDialog";

export function ReviewMenu({ incident }: { incident: IncidentItem }) {
  const { t } = useI18n();
  const [status, setStatus] = useState<"ACKNOWLEDGED" | "RESOLVED" | null>(null);
  const actions = t.incidents.actions;
  const items = [
    ...(incident.status === "OPEN" ? [{ label: actions.acknowledge, onSelect: () => setStatus("ACKNOWLEDGED" as const) }] : []),
    ...(incident.status !== "RESOLVED" ? [{ label: actions.resolve, onSelect: () => setStatus("RESOLVED" as const) }] : []),
  ];

  if (items.length === 0) {
    return null;
  }

  return (
    <div onClick={(event) => event.stopPropagation()}>
      <ActionMenu label={actions.menu} items={items} />
      {status && <ReviewDialog incident={incident} status={status} onClose={() => setStatus(null)} />}
    </div>
  );
}
