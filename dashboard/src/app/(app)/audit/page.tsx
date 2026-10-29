import type { Metadata } from "next";
import { AuditPage } from "@/features/audit/components/AuditPage";

export const metadata: Metadata = {
  title: "Journal d'audit · Zella Charging",
};

export default function Audit() {
  return <AuditPage />;
}
