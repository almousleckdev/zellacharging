import type { Metadata } from "next";
import { CustomerDetailPage } from "@/features/customers/components/CustomerDetailPage";

export const metadata: Metadata = {
  title: "Client · Zella Charging",
};

export default function CustomerDetail() {
  return <CustomerDetailPage />;
}
