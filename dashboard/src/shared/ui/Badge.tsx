import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

type Tone = "neutral" | "success" | "warning";

const TONES: Record<Tone, string> = {
  neutral: "bg-surface-strong text-muted",
  success: "bg-success-soft text-success",
  warning: "bg-primary-soft text-primary-text",
};

export function Badge({ tone = "neutral", children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span className={cn("inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium", TONES[tone])}>
      {children}
    </span>
  );
}
