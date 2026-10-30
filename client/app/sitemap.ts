import type { MetadataRoute } from "next";
import { locales, site } from "@/lib/site";

const pages = ["", "/privacy", "/terms", "/delete-account", "/contact"];

export default function sitemap(): MetadataRoute.Sitemap {
  return locales.flatMap((lang) =>
    pages.map((page) => ({ url: `${site.url}/${lang}${page}`, changeFrequency: "monthly" as const })),
  );
}
