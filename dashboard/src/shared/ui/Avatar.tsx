"use client";

import Image from "next/image";
import { useState } from "react";
import { cn } from "@/shared/lib/cn";

const HUES = [
  "bg-orange-200 text-orange-900",
  "bg-indigo-200 text-indigo-900",
  "bg-teal-200 text-teal-900",
  "bg-rose-200 text-rose-900",
  "bg-emerald-200 text-emerald-900",
  "bg-violet-200 text-violet-900",
];

function hueOf(initials: string): string {
  return HUES[[...initials].reduce((sum, char) => sum + char.charCodeAt(0), 0) % HUES.length];
}

type AvatarProps = {
  initials: string;
  src?: string | null;
  size?: number;
  className?: string;
};

export function Avatar({ initials, src, size = 36, className }: AvatarProps) {
  const [failedSrc, setFailedSrc] = useState<string | null>(null);
  const showImage = Boolean(src) && failedSrc !== src;

  return (
    <span
      aria-hidden="true"
      style={{ width: size, height: size }}
      className={cn(
        "flex shrink-0 items-center justify-center overflow-hidden rounded-full font-display text-xs font-semibold",
        !showImage && hueOf(initials),
        className,
      )}
    >
      {showImage && src ? (
        <Image src={src} alt="" width={size} height={size} unoptimized onError={() => setFailedSrc(src)} className="size-full object-cover" />
      ) : (
        initials
      )}
    </span>
  );
}
