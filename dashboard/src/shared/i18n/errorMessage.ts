import type { Dictionary } from "./types";

export function errorMessage(t: Dictionary, code: string): string {
  const messages: Record<string, string> = t.errors;
  return messages[code] ?? t.errors.UNKNOWN;
}
