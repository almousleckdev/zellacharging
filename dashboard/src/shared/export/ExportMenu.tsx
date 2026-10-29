"use client";

import { useState } from "react";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { ActionMenu } from "@/shared/ui/ActionMenu";
import { useToast } from "@/shared/ui/ToastProvider";
import { exportPdf } from "./toPdf";
import { exportXlsx } from "./toXlsx";
import type { ExportSheet } from "./types";

type ExportMenuProps<Row> = {
  load: () => Promise<{ rows: Row[]; truncated: boolean }>;
  sheet: (rows: Row[]) => ExportSheet<Row>;
  limit: number;
};

export function ExportMenu<Row>({ load, sheet, limit }: ExportMenuProps<Row>) {
  const { t } = useI18n();
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const labels = t.common.export;

  const run = async (write: (sheet: ExportSheet<Row>) => Promise<void>) => {
    setBusy(true);
    try {
      const { rows, truncated } = await load();
      await write(sheet(rows));
      if (truncated) {
        toast.notify("success", interpolate(labels.truncated, { count: limit }));
      }
    } catch {
      toast.notify("error", labels.failed);
    } finally {
      setBusy(false);
    }
  };

  if (busy) {
    return (
      <Button variant="secondary" className="h-10" loading disabled>
        {labels.preparing}
      </Button>
    );
  }

  return (
    <ActionMenu
      label={labels.button}
      trigger={labels.button}
      items={[
        { label: labels.excel, onSelect: () => void run(exportXlsx) },
        { label: labels.pdf, onSelect: () => void run(exportPdf) },
      ]}
    />
  );
}
