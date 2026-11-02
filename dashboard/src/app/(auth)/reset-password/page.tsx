import type { Metadata } from "next";
import { Suspense } from "react";
import { AuthLayout } from "@/features/auth/components/AuthLayout";
import { ResetPasswordForm } from "@/features/auth/components/ResetPasswordForm";

export const metadata: Metadata = {
  title: "Nouveau mot de passe · Zella Charging",
  referrer: "no-referrer",
};

export default function ResetPasswordPage() {
  return (
    <AuthLayout>
      <Suspense fallback={null}>
        <ResetPasswordForm />
      </Suspense>
    </AuthLayout>
  );
}
