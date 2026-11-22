import type { Agreement } from "./schemas";

export type FreeChargingDialog = { kind: "form"; agreement: Agreement | null } | { kind: "revoke"; agreement: Agreement } | null;
