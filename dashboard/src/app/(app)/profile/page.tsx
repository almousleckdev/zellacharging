import type { Metadata } from "next";
import { ProfilePage } from "@/features/profile/components/ProfilePage";

export const metadata: Metadata = {
  title: "Mon profil · Zella Charging",
};

export default function Profile() {
  return <ProfilePage />;
}
