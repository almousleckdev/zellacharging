"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Checkbox } from "@/shared/ui/Checkbox";
import { Select } from "@/shared/ui/Select";
import { ASSIGNABLE_PERMISSIONS, ASSIGNABLE_ROLES, ROLE_PRESETS } from "../schemas";
import type { AccessDefinition, AssignableRole } from "../schemas";

type AccessFieldsProps = {
  value: AccessDefinition;
  countriesError?: string;
  disabled: boolean;
  onChange: (value: AccessDefinition) => void;
};

function toggled(list: string[], item: string, on: boolean): string[] {
  return on ? [...list, item] : list.filter((entry) => entry !== item);
}

export function AccessFields({ value, countriesError, disabled, onChange }: AccessFieldsProps) {
  const { t } = useI18n();
  const { profile } = useSession();
  const roles: Record<string, string> = t.shell.roles;
  const hints: Record<string, string> = t.users.roleHints;
  const permissions: Record<string, string> = t.profile.permissionLabels;
  const labels = t.users.form;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1.5">
        <Select
          label={labels.role}
          value={value.role}
          disabled={disabled}
          options={ASSIGNABLE_ROLES.map((role) => ({ value: role, label: roles[role] }))}
          onChange={(event) => {
            const role = event.target.value as AssignableRole;
            onChange({ ...value, role, permissions: [...ROLE_PRESETS[role]] });
          }}
        />
        <p className="text-xs text-subtle">{hints[value.role]}</p>
      </div>
      <fieldset className="flex flex-col gap-2.5" disabled={disabled}>
        <legend className="text-sm font-medium text-foreground">{labels.countries}</legend>
        <p className="text-xs text-subtle">{labels.countriesHint}</p>
        {profile.countries.map((country) => (
          <Checkbox
            key={country.code}
            label={country.name}
            checked={value.countries.includes(country.code)}
            onChange={(event) => onChange({ ...value, countries: toggled(value.countries, country.code, event.target.checked) })}
          />
        ))}
        {countriesError && <p className="text-xs text-danger">{countriesError}</p>}
      </fieldset>
      <fieldset className="flex flex-col gap-2.5" disabled={disabled}>
        <legend className="text-sm font-medium text-foreground">{labels.permissions}</legend>
        <p className="text-xs text-subtle">{labels.permissionsHint}</p>
        <div className="grid gap-2.5 sm:grid-cols-2">
          {ASSIGNABLE_PERMISSIONS.map((permission) => (
            <Checkbox
              key={permission}
              label={permissions[permission]}
              checked={value.permissions.includes(permission)}
              onChange={(event) =>
                onChange({ ...value, permissions: toggled(value.permissions, permission, event.target.checked) })
              }
            />
          ))}
        </div>
      </fieldset>
    </div>
  );
}
