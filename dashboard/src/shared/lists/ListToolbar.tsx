import type { ReactNode } from "react";

type ListToolbarProps = {
  search: ReactNode;
  filters?: ReactNode;
  actions?: ReactNode;
  range?: ReactNode;
};

export function ListToolbar({ search, filters, actions, range }: ListToolbarProps) {
  return (
    <div className="flex flex-col gap-3 border-b border-border px-4 py-3.5 sm:flex-row sm:flex-wrap sm:items-center sm:px-5">
      {search}
      {filters}
      {range}
      <div className="sm:ml-auto">{actions}</div>
    </div>
  );
}
