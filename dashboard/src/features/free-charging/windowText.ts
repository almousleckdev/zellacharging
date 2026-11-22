import type { Dictionary } from "@/shared/i18n/types";
import { interpolate } from "@/shared/i18n/interpolate";
import type { FreeWindow } from "./schemas";

export function formatTime(time: string, locale: "fr" | "en"): string {
  const [hours, minutes] = time.split(":");
  return locale === "fr" ? `${hours}h${minutes}` : `${hours}:${minutes}`;
}

export function describeWindow(t: Dictionary, locale: "fr" | "en", window: FreeWindow): string {
  const labels = t.freeCharging.window;
  const days: Record<string, string> = labels.days;
  return interpolate(labels.sentence, {
    day: days[window.day] ?? window.day,
    from: formatTime(window.from, locale),
    until: formatTime(window.until ?? "23:59", locale),
  });
}
