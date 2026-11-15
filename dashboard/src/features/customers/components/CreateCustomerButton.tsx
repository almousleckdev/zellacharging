"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { CreateCustomerDialog } from "./CreateCustomerDialog";

export function CreateCustomerButton() {
  const { t } = useI18n();
  const { profile } = useSession();
  const [open, setOpen] = useState(false);

  if (!profile.permissions.includes("CUSTOMERS_MANAGE")) {
    return null;
  }

  return (
    <>
      <Button onClick={() => setOpen(true)}>{t.customers.create.button}</Button>
      {open && <CreateCustomerDialog onClose={() => setOpen(false)} />}
    </>
  );
}
