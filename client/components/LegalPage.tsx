import type { LegalDocument } from "@/content/legal-types";
import { site } from "@/lib/site";

export function LegalPage({ doc, children }: { doc: LegalDocument; children?: React.ReactNode }) {
  return (
    <article className="mx-auto max-w-3xl px-4 py-14 sm:px-6 sm:py-20">
      <h1 className="text-3xl font-extrabold tracking-tight sm:text-4xl">{doc.title}</h1>
      <p className="mt-2 text-sm text-muted">
        {doc.updatedLabel} : {site.updated}
      </p>
      <p className="mt-8 text-lg leading-relaxed text-muted">{doc.intro}</p>
      {children}
      <div className="mt-10 space-y-10">
        {doc.sections.map((section) => (
          <section key={section.heading}>
            <h2 className="text-xl font-bold">{section.heading}</h2>
            {section.paragraphs?.map((text) => (
              <p key={text} className="mt-3 leading-relaxed text-muted">
                {text}
              </p>
            ))}
            {section.bullets && (
              <ul className="mt-3 list-disc space-y-2 pl-5 leading-relaxed text-muted marker:text-brand">
                {section.bullets.map((text) => (
                  <li key={text}>{text}</li>
                ))}
              </ul>
            )}
          </section>
        ))}
      </div>
    </article>
  );
}
