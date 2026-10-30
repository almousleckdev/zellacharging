import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { LegalPage } from "@/components/LegalPage";
import { deleteAccountFor } from "@/content/legal";
import { isLocale, locales } from "@/lib/site";

export function generateStaticParams() {
  return locales.map((lang) => ({ lang }));
}

export async function generateMetadata({ params }: PageProps<"/[lang]/delete-account">): Promise<Metadata> {
  const { lang } = await params;
  return isLocale(lang) ? { title: deleteAccountFor(lang).title } : {};
}

export default async function Page({ params }: PageProps<"/[lang]/delete-account">) {
  const { lang } = await params;
  if (!isLocale(lang)) notFound();
  return <LegalPage doc={deleteAccountFor(lang)} />;
}
