import type { Copy } from "@/content/types";
import { Icon } from "./Icon";
import { PhoneMockup } from "./PhoneMockup";
import { StoreButtons } from "./StoreButtons";

export function Hero({ copy }: { copy: Copy }) {
  return (
    <section className="relative overflow-hidden bg-gradient-to-b from-brand-soft via-white to-white">
      <div className="mx-auto grid max-w-6xl items-center gap-12 px-4 pb-16 pt-14 sm:px-6 md:grid-cols-[1.1fr_0.9fr] md:pb-24 md:pt-20">
        <div className="space-y-7">
          <span className="inline-flex items-center gap-2 rounded-full border border-brand/30 bg-white px-3 py-1 text-xs font-semibold text-brand-deep">
            <Icon name="bolt" className="h-3.5 w-3.5" />
            {copy.hero.badge}
          </span>
          <h1 className="text-4xl font-extrabold leading-[1.1] tracking-tight sm:text-5xl lg:text-6xl">
            {copy.hero.title} <span className="text-brand">{copy.hero.highlight}</span>
          </h1>
          <p className="max-w-xl text-lg leading-relaxed text-muted">{copy.hero.text}</p>
          <StoreButtons copy={copy.stores} />
          <ul className="flex flex-wrap gap-x-6 gap-y-2 text-sm text-muted">
            {copy.hero.trust.map((item) => (
              <li key={item} className="inline-flex items-center gap-2">
                <Icon name="check" className="h-4 w-4 text-charge" />
                {item}
              </li>
            ))}
          </ul>
        </div>
        <PhoneMockup copy={copy.phone} />
      </div>
    </section>
  );
}
