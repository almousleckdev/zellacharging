import type { Copy } from "@/content/types";
import { Icon, type IconName } from "./Icon";

const icons: IconName[] = ["map", "scan", "bolt", "wallet", "bell", "shield"];

export function Features({ copy }: { copy: Copy["features"] }) {
  return (
    <section id="features" className="scroll-mt-20 border-t border-line py-20">
      <div className="mx-auto max-w-6xl px-4 sm:px-6">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">{copy.title}</h2>
          <p className="mt-4 text-lg text-muted">{copy.text}</p>
        </div>
        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {copy.items.map((item, index) => (
            <article key={item.title} className="rounded-2xl border border-line bg-white p-6 transition-colors hover:border-brand">
              <span className="inline-flex h-11 w-11 items-center justify-center rounded-xl border border-line text-brand">
                <Icon name={icons[index]} />
              </span>
              <h3 className="mt-5 text-lg font-semibold">{item.title}</h3>
              <p className="mt-2 leading-relaxed text-muted">{item.text}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
