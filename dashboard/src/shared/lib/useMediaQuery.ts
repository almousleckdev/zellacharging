"use client";

import { useSyncExternalStore } from "react";

export function useMediaQuery(query: string, fallback: boolean): boolean {
  const subscribe = (notify: () => void) => {
    if (typeof window.matchMedia !== "function") {
      return () => undefined;
    }
    const list = window.matchMedia(query);
    list.addEventListener("change", notify);
    return () => list.removeEventListener("change", notify);
  };
  const snapshot = () => (typeof window.matchMedia === "function" ? window.matchMedia(query).matches : fallback);

  return useSyncExternalStore(subscribe, snapshot, () => fallback);
}
