import type { Metadata } from "next";
import { PaymentsByCustomerPage } from "@/features/payments/components/PaymentsByCustomerPage";

export const metadata: Metadata = {
  title: "Paiements · Zella Charging",
};

export default function Payments() {
  return <PaymentsByCustomerPage />;
}
