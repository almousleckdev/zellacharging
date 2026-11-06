import type { ReactNode } from "react";
import { Icon } from "./Icon";

export function Alert({ children }: { children: ReactNode }) {
  return (
    <div
      role="alert"
      className="flex items-start gap-2.5 rounded-control border border-danger-border bg-danger-soft px-3 py-2.5 text-sm text-danger"
    >
      <Icon name="alert" className="mt-0.5 size-4" />
      <p>{children}</p>
    </div>
  );
}
