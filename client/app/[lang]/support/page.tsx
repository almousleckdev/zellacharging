import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { supportFor } from "@/content/support";
import { isLocale, locales, site } from "@/lib/site";

export function generateStaticParams() {
  return locales.map((lang) => ({ lang }));
}

export async function generateMetadata({ params }: PageProps<"/[lang]/support">): Promise<Metadata> {
  const { lang } = await params;
  return isLocale(lang) ? { title: supportFor(lang).title } : {};
}

export default async function SupportPage({ params }: PageProps<"/[lang]/support">) {
  const { lang } = await params;
  if (!isLocale(lang)) notFound();
  const copy = supportFor(lang);
  return (
    <section className="mx-auto max-w-3xl px-4 py-14 sm:px-6 sm:py-20">
      <h1 className="text-3xl font-extrabold tracking-tight sm:text-4xl">{copy.title}</h1>
      <p className="mt-6 text-lg leading-relaxed text-muted">{copy.text}</p>
      <dl className="mt-10 divide-y divide-line rounded-2xl border border-line">
        <div className="flex flex-col gap-1 p-5 sm:flex-row sm:items-center sm:justify-between">
          <dt className="font-semibold">{copy.email}</dt>
          <dd>
            <a className="text-brand-deep hover:underline" href={`mailto:${site.supportEmail}`}>
              {site.supportEmail}
            </a>
          </dd>
        </div>
        {site.supportPhone && (
          <div className="flex flex-col gap-1 p-5 sm:flex-row sm:items-center sm:justify-between">
            <dt className="font-semibold">{copy.phone}</dt>
            <dd>
              <a className="text-brand-deep hover:underline" href={`tel:${site.supportPhone}`}>
                {site.supportPhone}
              </a>
            </dd>
          </div>
        )}
      </dl>
      <p className="mt-6 text-sm text-muted">{copy.include}</p>
    </section>
  );
}
