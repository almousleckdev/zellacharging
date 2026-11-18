import type { Metadata } from "next";
import { StaffUsagePage } from "@/features/staff/components/StaffUsagePage";

export const metadata: Metadata = {
  title: "Personnel NDZela · Zella Charging",
};

export default function Staff() {
  return <StaffUsagePage />;
}
