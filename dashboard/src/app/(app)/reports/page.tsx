import type { Metadata } from "next";
import { ReportsPage } from "@/features/reports/components/ReportsPage";

export const metadata: Metadata = {
  title: "Rapports · Zella Charging",
};

export default function Page() {
  return <ReportsPage />;
}
