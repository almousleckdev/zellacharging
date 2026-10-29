"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Badge } from "@/shared/ui/Badge";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";
import type { SettingsSection } from "../schemas";

type SettingsCardProps = { section: SettingsSection; currency: string; decimals: number };

export function SettingsCard({ section, currency, decimals }: SettingsCardProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels: Record<string, string> = t.platform.settings;
  const sections: Record<string, string> = t.platform.sections;

  const show = (key: string, value: string | number | boolean | null) => {
    if (value === null || value === "") {
      return t.common.none;
    }
    if (typeof value === "boolean") {
      return <Badge tone={value ? "success" : "neutral"}>{value ? t.platform.on : t.platform.off}</Badge>;
    }
    if (typeof value === "number" && key.endsWith("Minor")) {
      return format.money([{ currency, decimals, minor: value }])[0];
    }
    if (typeof value === "number" && key.endsWith("Percent")) {
      return `${format.number(value, value % 1 === 0 ? 0 : 1)} %`;
    }
    if (typeof value === "number" && key.endsWith("Seconds")) {
      return `${format.number(value)} s`;
    }
    if (key === "reportingStart") {
      return format.dateTime(String(value));
    }
    return String(value);
  };

  return (
    <Card title={sections[section.section] ?? section.section}>
      <FactList facts={Object.entries(section.values).map(([key, value]) => ({ label: labels[key] ?? key, value: show(key, value) }))} />
    </Card>
  );
}
