export type CountryScope = string;

export const queryKeys = {
  profile: ["admin", "profile"] as const,
  scoped: (country: CountryScope, ...parts: string[]) => ["admin", "scoped", country, ...parts] as const,
};
