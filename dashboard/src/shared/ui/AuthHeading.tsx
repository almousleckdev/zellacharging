import type { ReactNode } from "react";

type AuthHeadingProps = {
  title: string;
  subtitle?: string;
  children?: ReactNode;
};

export function AuthHeading({ title, subtitle, children }: AuthHeadingProps) {
  return (
    <div className="flex flex-col gap-1.5">
      <h1 className="font-display text-2xl font-semibold tracking-tight text-foreground">{title}</h1>
      {subtitle && <p className="text-sm text-muted">{subtitle}</p>}
      {children}
    </div>
  );
}
