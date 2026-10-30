import Image from "next/image";
import type { Copy } from "@/content/types";
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
          <div className="flex items-center gap-3 text-sm text-muted">
            <Image src="/airtel.png" alt="Airtel Money" width={36} height={36} className="h-9 w-9 rounded-lg" />
            <Image src="/moov.png" alt="Moov Money" width={36} height={36} className="h-9 w-9 rounded-lg" />
            <span>Airtel Money · Moov Money</span>
          </div>
        </div>
        <Image
          src="/screens/hero.png"
          alt=""
          width={751}
          height={1257}
          priority
          sizes="(min-width: 768px) 420px, 80vw"
          className="mx-auto h-auto w-full max-w-[300px] md:max-w-[400px]"
        />
      </div>
    </section>
  );
}
