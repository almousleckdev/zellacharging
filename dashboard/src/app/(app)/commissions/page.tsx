import type { Metadata } from "next";
import { CommissionsPage } from "@/features/commissions/components/CommissionsPage";

export const metadata: Metadata = {
  title: "Commissions · Zella Charging",
};

export default function Commissions() {
  return <CommissionsPage />;
}
