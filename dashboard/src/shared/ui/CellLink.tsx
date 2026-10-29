import Link from "next/link";
import type { ReactNode } from "react";
import { cn } from "@/shared/lib/cn";

type CellLinkProps = { href: string; className?: string; children: ReactNode };

export function CellLink({ href, className, children }: CellLinkProps) {
  return (
    <Link href={href} onClick={(event) => event.stopPropagation()} className={cn("hover:text-primary-text", className)}>
      {children}
    </Link>
  );
}
