import type { Metadata } from "next";
import { FreeChargingPage } from "@/features/free-charging/components/FreeChargingPage";

export const metadata: Metadata = {
  title: "Gratuité · Zella Charging",
};

export default function FreeCharging() {
  return <FreeChargingPage />;
}
