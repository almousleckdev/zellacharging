import fs from "node:fs";
import path from "node:path";
import Image from "next/image";

const available = (slot: number) =>
  fs.existsSync(path.join(process.cwd(), "public", "screens", `${slot}.png`));

export function Screenshot({ slot, className = "" }: { slot: number; className?: string }) {
  const frame = `relative aspect-[9/19.5] w-full overflow-hidden rounded-[2rem] border border-line bg-white ${className}`;
  if (!available(slot)) {
    return <div className={`${frame} border-dashed`} data-slot={slot} aria-hidden="true" />;
  }
  return (
    <div className={frame} data-slot={slot}>
      <Image src={`/screens/${slot}.png`} alt="" fill sizes="(min-width: 1024px) 280px, 60vw" className="object-cover" />
    </div>
  );
}
