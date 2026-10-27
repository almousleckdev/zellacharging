import type { Metadata } from "next";
import { CustomersPage } from "@/features/customers/components/CustomersPage";

export const metadata: Metadata = {
  title: "Clients · Zella Charging",
};

export default function Customers() {
  return <CustomersPage />;
}
