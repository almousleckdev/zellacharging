import Image from "next/image";
import type { Copy } from "@/content/types";

export function HowItWorks({ copy, eyebrow }: { copy: Copy["how"]; eyebrow: string }) {
  return (
    <section id="how" className="scroll-mt-20 border-t border-line py-20">
      <div className="mx-auto grid max-w-6xl items-center gap-12 px-4 sm:px-6 lg:grid-cols-2">
        <div className="relative aspect-[4/3] overflow-hidden rounded-3xl">
          <Image
            src="/station-gabon.png"
            alt=""
            fill
            sizes="(min-width: 1024px) 560px, 100vw"
            className="object-cover"
          />
        </div>
        <div>
          <p className="text-sm font-semibold uppercase tracking-wider text-brand">{eyebrow}</p>
          <h2 className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">{copy.title}</h2>
          <ol className="mt-10 space-y-8">
            {copy.steps.map((step, index) => (
              <li key={step.title} className="flex gap-5">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand font-bold text-white">
                  {index + 1}
                </span>
                <div>
                  <h3 className="text-lg font-semibold">{step.title}</h3>
                  <p className="mt-1 leading-relaxed text-muted">{step.text}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  );
}
