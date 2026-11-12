import Image from "next/image";
import type { Copy } from "@/content/types";
import { StoreButtons } from "./StoreButtons";

export function Hero({ copy }: { copy: Copy }) {
  return (
    <section className="overflow-hidden">
      <div className="mx-auto grid max-w-6xl gap-12 px-4 sm:px-6 md:min-h-[calc(100svh-4rem)] md:grid-cols-[1.1fr_0.9fr]">
        <div className="space-y-7 py-14 md:self-center">
          <h1 className="text-4xl font-extrabold leading-[1.1] tracking-tight sm:text-5xl lg:text-6xl">
            {copy.hero.title} <span className="text-brand">{copy.hero.highlight}</span>
          </h1>
          <p className="max-w-xl text-lg leading-relaxed text-muted">{copy.hero.text}</p>
          <StoreButtons copy={copy.stores} />
        </div>
        <div className="relative h-[480px] md:my-12 md:h-[min(620px,calc(100svh-10rem))] md:self-center">
          <Image
            src="/screens/hero.png"
            alt=""
            fill
            priority
            quality={95}
            sizes="(min-width: 768px) 320px, 80vw"
            className="object-contain"
          />
        </div>
      </div>
    </section>
  );
}
