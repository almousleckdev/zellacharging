import { interpolate } from "@/shared/i18n/interpolate";
import { cn } from "@/shared/lib/cn";

type StepperProps = {
  steps: string[];
  current: number;
  label: string;
};

export function Stepper({ steps, current, label }: StepperProps) {
  return (
    <div className="flex flex-col gap-2">
      <p className="text-xs font-medium text-subtle">{interpolate(label, { current: current + 1, total: steps.length })}</p>
      <ol className="flex gap-2">
        {steps.map((step, index) => (
          <li key={step} aria-current={index === current ? "step" : undefined} className="flex flex-1 flex-col gap-1.5">
            <span className={cn("h-1 rounded-full", index <= current ? "bg-primary" : "bg-surface-strong")} />
            <span className={cn("text-xs", index === current ? "font-semibold text-foreground" : "text-subtle")}>{step}</span>
          </li>
        ))}
      </ol>
    </div>
  );
}
