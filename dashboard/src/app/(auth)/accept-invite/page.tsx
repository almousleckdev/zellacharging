import type { Metadata } from "next";
import { Suspense } from "react";
import { AuthLayout } from "@/features/auth/components/AuthLayout";
import { AcceptInvitePage } from "@/features/onboarding/components/AcceptInvitePage";

export const metadata: Metadata = {
  title: "Invitation · Zella Charging",
  referrer: "no-referrer",
};

export default function AcceptInvite() {
  return (
    <AuthLayout>
      <Suspense fallback={null}>
        <AcceptInvitePage />
      </Suspense>
    </AuthLayout>
  );
}
