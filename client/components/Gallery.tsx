import type { Copy } from "@/content/types";
import { Screenshot } from "./Screenshot";

const slots = [2, 3, 4, 5, 6, 7];

export function Gallery({ copy }: { copy: Copy["gallery"] }) {
  return (
    <section id="screens" className="scroll-mt-20 border-t border-line py-20">
      <div className="mx-auto max-w-6xl px-4 sm:px-6">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">{copy.title}</h2>
          <p className="mt-4 text-lg text-muted">{copy.text}</p>
        </div>
        <div className="-mx-4 mt-14 flex snap-x gap-5 overflow-x-auto px-4 pb-4 sm:mx-0 sm:grid sm:grid-cols-3 sm:overflow-visible sm:px-0 lg:grid-cols-6">
          {slots.map((slot) => (
            <div key={slot} className="w-[58%] shrink-0 snap-center sm:w-auto">
              <Screenshot slot={slot} />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
