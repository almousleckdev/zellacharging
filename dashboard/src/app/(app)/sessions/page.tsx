import type { Metadata } from "next";
import { SessionsByCustomerPage } from "@/features/sessions/components/SessionsByCustomerPage";

export const metadata: Metadata = {
  title: "Sessions · Zella Charging",
};

export default function Sessions() {
  return <SessionsByCustomerPage />;
}
