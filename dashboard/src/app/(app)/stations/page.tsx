import type { Metadata } from "next";
import { StationsPage } from "@/features/stations/components/StationsPage";

export const metadata: Metadata = {
  title: "Bornes · Zella Charging",
};

export default function Stations() {
  return <StationsPage />;
}
