import Image from "next/image";
import Link from "next/link";
import type { Copy } from "@/content/types";
import type { Locale } from "@/lib/site";

export function Header({ lang, copy }: { lang: Locale; copy: Copy }) {
  const links = [
    { href: `/${lang}#features`, label: copy.nav.features },
    { href: `/${lang}#how`, label: copy.nav.how },
    { href: `/${lang}#faq`, label: copy.nav.faq },
    { href: `/${lang}/contact`, label: copy.nav.contact },
  ];
  return (
    <header className="sticky top-0 z-30 border-b border-line bg-white/85 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Link href={`/${lang}`} aria-label="Zella Charging">
          <Image src="/logo.png" alt="Zella" width={118} height={27} priority className="h-6 w-auto" />
        </Link>
        <nav className="hidden items-center gap-8 text-sm font-medium text-muted md:flex">
          {links.map((link) => (
            <Link key={link.href} href={link.href} className="transition-colors hover:text-ink">
              {link.label}
            </Link>
          ))}
        </nav>
        <Link
          href={`/${copy.switchTo.locale}`}
          className="rounded-lg border border-line px-3 py-1.5 text-sm font-medium text-ink transition-colors hover:border-brand hover:text-brand-deep"
          hrefLang={copy.switchTo.locale}
        >
          {copy.switchTo.label}
        </Link>
      </div>
    </header>
  );
}
