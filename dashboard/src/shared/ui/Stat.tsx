type StatProps = { label: string; value: string; hint?: string };

export function Stat({ label, value, hint }: StatProps) {
  return (
    <div className="flex flex-col gap-1 rounded-card border border-border bg-background px-4 py-3.5">
      <p className="text-xs font-medium uppercase tracking-wide text-subtle">{label}</p>
      <p className="figure font-display text-xl font-semibold text-foreground">{value}</p>
      {hint && <p className="text-xs text-subtle">{hint}</p>}
    </div>
  );
}
