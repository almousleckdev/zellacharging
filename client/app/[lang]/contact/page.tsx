import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { ContactForm } from "@/components/ContactForm";
import { Icon, type IconName } from "@/components/Icon";
import { contactFor } from "@/content/contact";
import { isLocale, locales, site } from "@/lib/site";

export function generateStaticParams() {
  return locales.map((lang) => ({ lang }));
}

export async function generateMetadata({ params }: PageProps<"/[lang]/contact">): Promise<Metadata> {
  const { lang } = await params;
  return isLocale(lang) ? { title: contactFor(lang).title } : {};
}

export default async function ContactPage({ params }: PageProps<"/[lang]/contact">) {
  const { lang } = await params;
  if (!isLocale(lang)) notFound();
  const copy = contactFor(lang);
  const channels: { icon: IconName; label: string; hint: string; value: string; href: string }[] = [
    { icon: "phone", ...copy.phone, value: site.phone, href: `tel:${site.phone}` },
    { icon: "mail", ...copy.general, value: site.contactEmail, href: `mailto:${site.contactEmail}` },
    { icon: "mail", ...copy.team, value: site.teamEmail, href: `mailto:${site.teamEmail}` },
    { icon: "shield", ...copy.privacy, value: site.privacyEmail, href: `mailto:${site.privacyEmail}` },
  ];
  return (
    <section className="mx-auto max-w-6xl px-4 py-14 sm:px-6 sm:py-20">
      <h1 className="text-3xl font-extrabold tracking-tight sm:text-4xl">{copy.title}</h1>
      <p className="mt-6 max-w-2xl text-lg leading-relaxed text-muted">{copy.text}</p>
      <div className="mt-12 grid gap-10 lg:grid-cols-[1fr_1.1fr]">
        <div>
          <ul className="space-y-4">
            {channels.map((channel) => (
              <li key={channel.href}>
                <a
                  href={channel.href}
                  className="flex gap-4 rounded-2xl border border-line p-5 transition-colors hover:border-brand"
                >
                  <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl border border-line text-brand">
                    <Icon name={channel.icon} />
                  </span>
                  <span className="min-w-0">
                    <span className="block font-semibold">{channel.label}</span>
                    <span className="block text-sm text-muted">{channel.hint}</span>
                    <span className="mt-1 block break-words font-medium text-brand-deep">{channel.value}</span>
                  </span>
                </a>
              </li>
            ))}
          </ul>
          <p className="mt-6 text-sm text-muted">{copy.include}</p>
        </div>
        <ContactForm lang={lang} copy={copy.form} />
      </div>
    </section>
  );
}
