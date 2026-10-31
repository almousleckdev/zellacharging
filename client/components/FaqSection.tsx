import type { Copy } from "@/content/types";
import { Icon } from "./Icon";

export function FaqSection({ copy, eyebrow }: { copy: Copy["faq"]; eyebrow: string }) {
  return (
    <section id="faq" className="scroll-mt-20 border-t border-line py-20">
      <div className="mx-auto max-w-3xl px-4 sm:px-6">
        <p className="text-center text-sm font-semibold uppercase tracking-wider text-brand">{eyebrow}</p>
        <h2 className="mt-2 text-center text-3xl font-bold tracking-tight sm:text-4xl">{copy.title}</h2>
        <div className="mt-12 space-y-3">
          {copy.items.map((item) => (
            <details key={item.question} className="group rounded-xl border border-line bg-white px-5 py-4">
              <summary className="flex cursor-pointer list-none items-center justify-between gap-4 font-semibold">
                {item.question}
                <Icon name="chevron" className="h-5 w-5 shrink-0 text-muted transition-transform group-open:rotate-180" />
              </summary>
              <p className="mt-3 leading-relaxed text-muted">{item.answer}</p>
            </details>
          ))}
        </div>
      </div>
    </section>
  );
}
