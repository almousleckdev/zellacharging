import type { CustomerItem } from "@/features/customers/schemas";
import { useI18n } from "@/shared/i18n/I18nProvider";

export function SelectedCustomer({ customer }: { customer: CustomerItem }) {
  const { t } = useI18n();
  const labels = t.freeCharging.picker;

  return (
    <div className="flex items-center justify-between gap-3 rounded-control border border-primary bg-surface px-3 py-2 text-sm">
      <span className="text-muted">{labels.selected}</span>
      <span className="font-medium text-foreground">{`${customer.firstName} ${customer.lastName}`}</span>
      <span className="text-muted">{customer.phone ?? labels.noPhone}</span>
    </div>
  );
}
