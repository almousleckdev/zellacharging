import Image from "next/image";
import type { Copy } from "@/content/types";

export function ChargeShowcase({ copy }: { copy: Copy["charge"] }) {
  return (
    <section className="border-t border-line py-20">
      <div className="mx-auto max-w-5xl px-4 text-center sm:px-6">
        <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">{copy.title}</h2>
        <p className="mx-auto mt-4 max-w-xl text-lg text-muted">{copy.text}</p>
        <Image
          src="/charging-scene.png"
          alt=""
          width={1910}
          height={720}
          sizes="(min-width: 1024px) 960px, 100vw"
          className="mx-auto mt-12 h-auto w-full max-w-4xl"
          style={{ animation: "glow 3s ease-in-out infinite alternate" }}
        />
        <div className="mx-auto mt-8 h-1.5 max-w-md overflow-hidden rounded-full bg-line" role="presentation">
          <div
            className="relative h-full origin-left overflow-hidden rounded-full bg-charge"
            style={{ animation: "fill 6s ease-in-out infinite" }}
          >
            <span
              className="absolute inset-y-0 left-0 w-1/4 bg-white/50"
              style={{ animation: "sweep 1.6s linear infinite" }}
            />
          </div>
        </div>
      </div>
    </section>
  );
}
