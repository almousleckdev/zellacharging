import type { Metadata } from "next";
import { SystemPage } from "@/features/system/components/SystemPage";

export const metadata: Metadata = {
  title: "Système · Zella Charging",
};

export default function Page() {
  return <SystemPage />;
}
