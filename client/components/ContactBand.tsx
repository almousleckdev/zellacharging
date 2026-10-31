import Link from "next/link";
import type { Copy } from "@/content/types";
import { site, type Locale } from "@/lib/site";

export function ContactBand({ lang, copy }: { lang: Locale; copy: Copy["contactBand"] }) {
  return (
    <section className="border-t border-line px-4 py-16 sm:px-6">
      <div className="mx-auto flex max-w-5xl flex-col items-start justify-between gap-6 md:flex-row md:items-center">
        <div>
          <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">{copy.title}</h2>
          <p className="mt-2 text-lg text-muted">{copy.text}</p>
        </div>
        <div className="flex flex-wrap items-center gap-4">
          <a href={`tel:${site.phone}`} className="font-semibold text-brand-deep hover:underline">
            {site.phone}
          </a>
          <Link
            href={`/${lang}/contact`}
            className="inline-flex h-12 items-center rounded-xl bg-brand px-6 font-semibold text-white transition-colors hover:bg-brand-deep"
          >
            {copy.button}
          </Link>
        </div>
      </div>
    </section>
  );
}
