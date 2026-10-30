import Image from "next/image";
import type { Copy } from "@/content/types";

export function Payments({ copy }: { copy: Copy["payments"] }) {
  return (
    <section className="py-16">
      <div className="mx-auto flex max-w-4xl flex-col items-center gap-6 px-4 text-center sm:px-6">
        <h2 className="text-2xl font-bold tracking-tight sm:text-3xl">{copy.title}</h2>
        <p className="max-w-xl text-muted">{copy.text}</p>
        <div className="flex items-center gap-6">
          <Image src="/airtel.png" alt="Airtel Money" width={64} height={64} className="h-14 w-14 rounded-xl" />
          <Image src="/moov.png" alt="Moov Money" width={64} height={64} className="h-14 w-14 rounded-xl" />
        </div>
      </div>
    </section>
  );
}
