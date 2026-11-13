"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import type { CountryFigures } from "../schemas";

export function ComplimentaryCard({ countries }: { countries: CountryFigures[] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.complimentary;
  const given = countries.filter((country) => country.sessions.complimentaryKwh > 0);
  if (given.length === 0) {
    return null;
  }

  return (
    <Card title={labels.title} description={labels.text}>
      <ul className="flex flex-col gap-4">
        {given.map((country) => (
          <li key={country.code} className="flex flex-col gap-1">
            {countries.length > 1 && <p className="text-sm font-semibold text-foreground">{country.name}</p>}
            <p className="flex justify-between text-sm text-muted">
              <span>{labels.energy}</span>
              <span className="figure font-semibold text-foreground">{format.number(country.sessions.complimentaryKwh, 1)} kWh</span>
            </p>
            <p className="flex justify-between text-sm text-muted">
              <span>{labels.value}</span>
              <span className="figure font-semibold text-foreground">
                {format.money([{ currency: country.currency, decimals: country.currencyDecimals, minor: country.sessions.complimentaryValueMinor }])[0]}
              </span>
            </p>
          </li>
        ))}
      </ul>
    </Card>
  );
}
