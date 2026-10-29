import type { Metadata } from "next";
import { TariffsPage } from "@/features/tariffs/components/TariffsPage";

export const metadata: Metadata = {
  title: "Tarifs · Zella Charging",
};

export default function Page() {
  return <TariffsPage />;
}
