"use client";

import Image from "next/image";
import { useState } from "react";
import { cn } from "@/shared/lib/cn";

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
        "flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-primary-soft text-xs font-semibold text-primary-text",
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
