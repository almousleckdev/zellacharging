"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { useFreeWindow } from "../api/useFreeWindow";
import { describeWindow } from "../windowText";
import { WindowDialog } from "./WindowDialog";

export function WindowCard() {
  const { t, locale } = useI18n();
  const { profile } = useSession();
  const [editing, setEditing] = useState(false);
  const window = useFreeWindow();
  const labels = t.freeCharging.window;
  const canManage = profile.permissions.includes("FREE_CHARGING_MANAGE");
  const sentence = window.data ? describeWindow(t, locale, window.data) : window.isError ? labels.loadError : labels.loading;
  const action = canManage && window.data ? <Button variant="secondary" onClick={() => setEditing(true)}>{labels.edit}</Button> : undefined;

  return (
    <>
      <Card title={labels.title} action={action}>
        <p className="px-4 py-3 text-sm text-subtle">{sentence}</p>
      </Card>
      {editing && window.data && <WindowDialog window={window.data} onClose={() => setEditing(false)} />}
    </>
  );
}
