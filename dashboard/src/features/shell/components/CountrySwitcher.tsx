"use client";

import { ALL_COUNTRIES } from "@/features/session/countryScope";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Icon } from "@/shared/ui/Icon";
import { Select } from "@/shared/ui/Select";

export function CountrySwitcher() {
  const { t } = useI18n();
  const { profile, country, setCountry } = useSession();
  const { countries } = profile;

  if (countries.length <= 1) {
    return (
      <span className="inline-flex h-9 items-center gap-2 rounded-control border border-border px-3 text-sm text-muted">
        <Icon name="globe" className="size-4" />
        {countries[0]?.name ?? t.shell.country.all}
      </span>
    );
  }

  const options = [
    { value: ALL_COUNTRIES, label: t.shell.country.all },
    ...countries.map((item) => ({ value: item.code, label: item.name })),
  ];

  return (
    <div className="flex items-center gap-2">
      <Icon name="globe" className="size-4 text-subtle" />
      <Select
        label={t.shell.country.label}
        hideLabel
        options={options}
        value={country}
        onChange={(event) => setCountry(event.target.value)}
        className="h-9 w-48"
      />
    </div>
  );
}
