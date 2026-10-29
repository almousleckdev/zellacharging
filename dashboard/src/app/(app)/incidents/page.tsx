import type { Metadata } from "next";
import { IncidentsPage } from "@/features/incidents/components/IncidentsPage";

export const metadata: Metadata = {
  title: "Incidents · Zella Charging",
};

export default function Incidents() {
  return <IncidentsPage />;
}
