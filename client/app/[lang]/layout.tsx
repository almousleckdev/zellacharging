import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Plus_Jakarta_Sans } from "next/font/google";
import { Footer } from "@/components/Footer";
import { Header } from "@/components/Header";
import { copyFor } from "@/content";
import { isLocale, locales, site } from "@/lib/site";
import "../globals.css";

const jakarta = Plus_Jakarta_Sans({ variable: "--font-jakarta", subsets: ["latin"] });

export function generateStaticParams() {
  return locales.map((lang) => ({ lang }));
}

export async function generateMetadata({ params }: LayoutProps<"/[lang]">): Promise<Metadata> {
  const { lang } = await params;
  if (!isLocale(lang)) return {};
  const copy = copyFor(lang);
  return {
    metadataBase: new URL(site.url),
    title: { default: copy.meta.title, template: `%s | ${site.name}` },
    description: copy.meta.description,
    alternates: { languages: { fr: "/fr", en: "/en" } },
    openGraph: { title: copy.meta.title, description: copy.meta.description, siteName: site.name, type: "website" },
  };
}

export default async function RootLayout({ children, params }: LayoutProps<"/[lang]">) {
  const { lang } = await params;
  if (!isLocale(lang)) notFound();
  const copy = copyFor(lang);
  return (
    <html lang={lang} className={jakarta.variable}>
      <body className="flex min-h-screen flex-col">
        <Header lang={lang} copy={copy} />
        <main className="flex-1">{children}</main>
        <Footer lang={lang} copy={copy} />
      </body>
    </html>
  );
}
