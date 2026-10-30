import Image from "next/image";
import Link from "next/link";
import type { Copy } from "@/content/types";
import { site, type Locale } from "@/lib/site";

export function Footer({ lang, copy }: { lang: Locale; copy: Copy }) {
  const columns = [
    {
      title: copy.footer.product,
      links: [
        { href: `/${lang}#features`, label: copy.nav.features },
        { href: `/${lang}#how`, label: copy.nav.how },
        { href: `/${lang}#faq`, label: copy.nav.faq },
      ],
    },
    {
      title: copy.footer.legal,
      links: [
        { href: `/${lang}/privacy`, label: copy.footer.privacy },
        { href: `/${lang}/terms`, label: copy.footer.terms },
        { href: `/${lang}/delete-account`, label: copy.footer.deleteAccount },
        { href: `/${lang}/support`, label: copy.footer.support },
      ],
    },
  ];
  return (
    <footer className="border-t border-line bg-surface-muted">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-12 sm:px-6 md:grid-cols-[1.5fr_1fr_1fr]">
        <div className="space-y-4">
          <Image src="/logo.png" alt="Zella" width={118} height={27} className="h-6 w-auto" />
          <p className="max-w-xs text-sm text-muted">{copy.meta.description}</p>
        </div>
        {columns.map((column) => (
          <div key={column.title}>
            <h3 className="text-sm font-semibold">{column.title}</h3>
            <ul className="mt-4 space-y-3 text-sm text-muted">
              {column.links.map((link) => (
                <li key={link.href}>
                  <Link href={link.href} className="transition-colors hover:text-brand-deep">
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="border-t border-line py-5 text-center text-xs text-muted">
        © {site.copyrightYear} {site.legalName}. {copy.footer.rights}
      </div>
    </footer>
  );
}
