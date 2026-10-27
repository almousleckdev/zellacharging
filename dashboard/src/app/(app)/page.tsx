import type { Metadata } from "next";
import { OverviewPage } from "@/features/overview/components/OverviewPage";

export const metadata: Metadata = {
  title: "Vue d'ensemble · Zella Charging",
};

export default function HomePage() {
  return <OverviewPage />;
}
