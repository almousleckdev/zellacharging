import type { Metadata } from "next";
import { WithdrawalDetailPage } from "@/features/withdrawals/components/WithdrawalDetailPage";

export const metadata: Metadata = {
  title: "Retrait · Zella Charging",
};

export default function WithdrawalDetail() {
  return <WithdrawalDetailPage />;
}
