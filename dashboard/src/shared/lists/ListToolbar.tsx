import type { ReactNode } from "react";

type ListToolbarProps = {
  search: ReactNode;
  filters?: ReactNode;
  actions?: ReactNode;
  range?: ReactNode;
};

export function ListToolbar({ search, filters, actions, range }: ListToolbarProps) {
  return (
    <div className="flex flex-wrap items-center gap-3 border-b border-border px-5 py-3.5">
      {search}
      {filters}
      {range}
      <div className="ml-auto">{actions}</div>
    </div>
  );
}
