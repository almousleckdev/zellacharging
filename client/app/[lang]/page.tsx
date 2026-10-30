import { notFound } from "next/navigation";
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
      <Features copy={copy.features} />
      <HowItWorks copy={copy.how} />
      <Payments copy={copy.payments} />
      <FaqSection copy={copy.faq} />
      <DownloadBand copy={copy} />
    </>
  );
}
