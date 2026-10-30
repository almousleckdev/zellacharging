import type { Copy } from "@/content/types";
import { Icon, type IconName } from "./Icon";
import { Screenshot } from "./Screenshot";

const icons: IconName[] = ["map", "scan", "bolt", "wallet", "bell", "shield"];

export function Features({ copy }: { copy: Copy["features"] }) {
  return (
    <section id="features" className="scroll-mt-20 border-t border-line py-20">
      <div className="mx-auto max-w-6xl px-4 sm:px-6">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">{copy.title}</h2>
          <p className="mt-4 text-lg text-muted">{copy.text}</p>
        </div>
        <div className="mt-16 space-y-20 md:space-y-28">
          {copy.items.map((item, index) => (
            <article key={item.title} className="grid items-center gap-10 md:grid-cols-2 md:gap-16">
              <div className={index % 2 === 1 ? "md:order-2" : ""}>
                <span className="inline-flex h-12 w-12 items-center justify-center rounded-xl border border-line text-brand">
                  <Icon name={icons[index]} className="h-6 w-6" />
                </span>
                <h3 className="mt-6 text-2xl font-bold tracking-tight sm:text-3xl">{item.title}</h3>
                <p className="mt-4 max-w-md text-lg leading-relaxed text-muted">{item.text}</p>
              </div>
              <div className="mx-auto w-full max-w-[260px]">
                <Screenshot slot={index + 2} />
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
