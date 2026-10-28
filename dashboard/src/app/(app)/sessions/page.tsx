import type { Metadata } from "next";
import { SessionsPage } from "@/features/sessions/components/SessionsPage";

export const metadata: Metadata = {
  title: "Sessions · Zella Charging",
};

export default function Sessions() {
  return <SessionsPage />;
}
