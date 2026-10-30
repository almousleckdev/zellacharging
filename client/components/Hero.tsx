import type { Copy } from "@/content/types";
import { Screenshot } from "./Screenshot";
import { StoreButtons } from "./StoreButtons";

export function Hero({ copy }: { copy: Copy }) {
  return (
    <section className="bg-white">
      <div className="mx-auto grid max-w-6xl items-center gap-12 px-4 pb-20 pt-14 sm:px-6 md:grid-cols-[1.1fr_0.9fr] md:pt-20">
        <div className="space-y-7">
          <h1 className="text-4xl font-extrabold leading-[1.1] tracking-tight sm:text-5xl lg:text-6xl">
            {copy.hero.title} <span className="text-brand">{copy.hero.highlight}</span>
          </h1>
          <p className="max-w-xl text-lg leading-relaxed text-muted">{copy.hero.text}</p>
          <StoreButtons copy={copy.stores} />
        </div>
        <div className="mx-auto w-full max-w-[280px]">
          <Screenshot slot={1} />
        </div>
      </div>
    </section>
  );
}
