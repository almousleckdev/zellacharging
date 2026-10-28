import type { Metadata } from "next";
import { PaymentsPage } from "@/features/payments/components/PaymentsPage";

export const metadata: Metadata = {
  title: "Paiements · Zella Charging",
};

export default function Payments() {
  return <PaymentsPage />;
}
