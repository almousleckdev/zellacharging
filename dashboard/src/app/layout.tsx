import type { Metadata } from "next";
import { Figtree } from "next/font/google";
import type { ReactNode } from "react";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
import "./globals.css";

const figtree = Figtree({
  variable: "--font-figtree",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Zella Charging · Console",
  description: "Console d'administration de la plateforme Zella Charging",
  robots: { index: false, follow: false },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="fr" className={`${figtree.variable} h-full`}>
      <body className="min-h-full">
        <I18nProvider>{children}</I18nProvider>
      </body>
    </html>
  );
}
