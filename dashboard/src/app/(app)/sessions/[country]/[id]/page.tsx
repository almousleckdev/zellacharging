import type { Metadata } from "next";
import { SessionDetailPage } from "@/features/sessions/components/SessionDetailPage";

export const metadata: Metadata = {
  title: "Recharge · Zella Charging",
};

export default function SessionDetail() {
  return <SessionDetailPage />;
}
