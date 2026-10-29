import type { Metadata } from "next";
import { StationDetailPage } from "@/features/stations/components/StationDetailPage";

export const metadata: Metadata = {
  title: "Borne · Zella Charging",
};

export default function StationDetail() {
  return <StationDetailPage />;
}
