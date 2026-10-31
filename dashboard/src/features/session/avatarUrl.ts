export function ownAvatarUrl(version: number | null): string | null {
  return version === null ? null : `/api/admin/me/avatar?v=${version}`;
}

export function userAvatarUrl(id: string, version: number | null): string | null {
  return version === null ? null : `/api/admin/users/${id}/avatar?v=${version}`;
}

export function customerAvatarUrl(country: string, id: string, version: number | null | undefined): string | null {
  return version === null || version === undefined ? null : `/api/admin/customers/${country}/${id}/avatar?v=${version}`;
}

export function initialsOf(firstName: string | null, lastName: string | null, email: string): string {
  const letters = `${firstName?.[0] ?? ""}${lastName?.[0] ?? ""}`;
  return (letters || email.slice(0, 2)).toUpperCase();
}

export function displayName(firstName: string | null, lastName: string | null, email: string): string {
  const name = `${firstName ?? ""} ${lastName ?? ""}`.trim();
  return name || email;
}
