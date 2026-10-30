import type { Copy } from "@/content/types";
import { StoreButtons } from "./StoreButtons";

export function DownloadBand({ copy }: { copy: Copy }) {
  return (
    <section className="px-4 py-20 sm:px-6">
      <div className="mx-auto max-w-5xl rounded-3xl bg-brand px-6 py-14 text-center text-white sm:px-12">
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">{copy.cta.title}</h2>
        <p className="mx-auto mt-4 max-w-xl text-lg text-white/90">{copy.cta.text}</p>
        <div className="mt-8 flex justify-center">
          <StoreButtons copy={copy.stores} tone="light" />
        </div>
      </div>
    </section>
  );
}
