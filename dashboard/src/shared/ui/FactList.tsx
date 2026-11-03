import type { ReactNode } from "react";

export type Fact = { label: string; value: ReactNode };

export function FactList({ facts }: { facts: Fact[] }) {
  return (
    <dl className="grid gap-4 text-sm">
      {facts.filter((fact) => fact.value !== "" && fact.value !== null && fact.value !== undefined).map((fact) => (
        <div key={fact.label} className="flex items-center justify-between gap-4">
          <dt className="text-muted">{fact.label}</dt>
          <dd className="min-w-0 text-right font-medium text-foreground">{fact.value}</dd>
        </div>
      ))}
    </dl>
  );
}
