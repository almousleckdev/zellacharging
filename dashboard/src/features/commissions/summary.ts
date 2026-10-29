import type { CommissionSummary } from "./schemas";

export function mergeSummaries(summaries: CommissionSummary[]): CommissionSummary[] {
  const groups = new Map<string, CommissionSummary>();
  for (const summary of summaries) {
    const known = groups.get(summary.currency);
    if (!known) {
      groups.set(summary.currency, { ...summary });
      continue;
    }
    groups.set(summary.currency, {
      ...known,
      country: `${known.country}, ${summary.country}`,
      sessions: known.sessions + summary.sessions,
      electricMinor: known.electricMinor + summary.electricMinor,
      serviceFeeMinor: known.serviceFeeMinor + summary.serviceFeeMinor,
      vatMinor: known.vatMinor + summary.vatMinor,
      serviceFeePercent: known.sessions >= summary.sessions ? known.serviceFeePercent : summary.serviceFeePercent,
    });
  }
  const merged = [...groups.values()];
  const active = merged.filter((summary) => summary.sessions > 0);
  return active.length > 0 ? active : merged.slice(0, 1);
}
