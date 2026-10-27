"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { Icon } from "@/shared/ui/Icon";

export function CountriesCard() {
  const { t } = useI18n();
  const { profile } = useSession();
  const labels = t.profile.countries;

  return (
    <Card title={labels.title} description={labels.subtitle}>
      {profile.countries.length === 0 ? (
        <p className="text-sm text-muted">{labels.empty}</p>
      ) : (
        <ul className="grid gap-3 sm:grid-cols-2">
          {profile.countries.map((country) => (
            <li key={country.code} className="flex items-center gap-3 rounded-control border border-border px-4 py-3">
              <Icon name="globe" className="text-primary-text" />
              <div className="flex flex-col">
                <span className="text-sm font-medium text-foreground">{country.name}</span>
                <span className="text-xs text-subtle">
                  {country.code} · {country.currency}
                </span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </Card>
  );
}
