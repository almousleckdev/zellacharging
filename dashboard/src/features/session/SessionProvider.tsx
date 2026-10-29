"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { createContext, useCallback, useContext, useMemo } from "react";
import type { ReactNode } from "react";
import { resolveCountry } from "./countryScope";
import type { AdminProfile } from "./schemas";

type SessionValue = {
  profile: AdminProfile;
  country: string;
  setCountry: (country: string) => void;
  scopedHref: (href: string) => string;
};

const SessionContext = createContext<SessionValue | null>(null);

export function SessionProvider({ profile, children }: { profile: AdminProfile; children: ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const country = resolveCountry(searchParams.get("country"), profile.countries);

  const setCountry = useCallback(
    (next: string) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set("country", next);
      router.replace(`${pathname}?${params.toString()}`);
    },
    [pathname, router, searchParams],
  );

  const scoped = searchParams.has("country");
  const scopedHref = useCallback(
    (href: string) => (scoped ? `${href}${href.includes("?") ? "&" : "?"}country=${encodeURIComponent(country)}` : href),
    [country, scoped],
  );

  const value = useMemo(
    () => ({ profile, country, setCountry, scopedHref }),
    [profile, country, setCountry, scopedHref],
  );

  return <SessionContext value={value}>{children}</SessionContext>;
}

export function useSession(): SessionValue {
  const value = useContext(SessionContext);
  if (!value) {
    throw new Error("useSession must be used within a SessionProvider");
  }
  return value;
}
