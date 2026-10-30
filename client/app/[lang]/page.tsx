import { notFound } from "next/navigation";
import { ContactBand } from "@/components/ContactBand";
import { DownloadBand } from "@/components/DownloadBand";
import { FaqSection } from "@/components/FaqSection";
import { Features } from "@/components/Features";
import { Hero } from "@/components/Hero";
import { HowItWorks } from "@/components/HowItWorks";
import { Payments } from "@/components/Payments";
import { copyFor } from "@/content";
import { isLocale } from "@/lib/site";

export default async function Home({ params }: PageProps<"/[lang]">) {
  const { lang } = await params;
  if (!isLocale(lang)) notFound();
  const copy = copyFor(lang);
  return (
    <>
      <Hero copy={copy} />
      <Features copy={copy.features} eyebrow={copy.nav.features} />
      <HowItWorks copy={copy.how} eyebrow={copy.nav.how} />
      <Payments copy={copy.payments} />
      <FaqSection copy={copy.faq} eyebrow={copy.nav.faq} />
      <DownloadBand copy={copy} />
      <ContactBand lang={lang} copy={copy.contactBand} />
    </>
  );
}
