import type { Metadata } from "next";
import { UsersPage } from "@/features/users/components/UsersPage";

export const metadata: Metadata = {
  title: "Utilisateurs · Zella Charging",
};

export default function Users() {
  return <UsersPage />;
}
