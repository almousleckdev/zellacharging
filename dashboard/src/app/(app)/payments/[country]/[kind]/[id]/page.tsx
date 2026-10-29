import type { Metadata } from "next";
import { PaymentDetailPage } from "@/features/payments/components/PaymentDetailPage";

export const metadata: Metadata = {
  title: "Paiement · Zella Charging",
};

export default function PaymentDetail() {
  return <PaymentDetailPage />;
}
