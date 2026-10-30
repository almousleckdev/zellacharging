import type { Metadata } from "next";
import { WithdrawalsPage } from "@/features/withdrawals/components/WithdrawalsPage";

export const metadata: Metadata = {
  title: "Retraits · Zella Charging",
};

export default function Withdrawals() {
  return <WithdrawalsPage />;
}
