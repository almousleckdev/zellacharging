import type { Metadata } from "next";
import { CountriesPage } from "@/features/countries/components/CountriesPage";

export const metadata: Metadata = {
  title: "Pays · Zella Charging",
};

export default function Page() {
  return <CountriesPage />;
}
