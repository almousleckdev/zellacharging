import Link from "next/link";
import type { ReactNode } from "react";
import { AuthHeading } from "@/shared/ui/AuthHeading";

type Props = {
  title: string;
  text: string;
  linkHref: string;
  linkLabel: string;
  children?: ReactNode;
};

export function AuthNotice({ title, text, linkHref, linkLabel, children }: Props) {
  return (
    <div className="flex flex-col gap-4">
      <AuthHeading title={title} subtitle={text} />
      {children}
      <Link href={linkHref} className="text-sm font-semibold text-primary-text hover:underline">
        {linkLabel}
      </Link>
    </div>
  );
}
