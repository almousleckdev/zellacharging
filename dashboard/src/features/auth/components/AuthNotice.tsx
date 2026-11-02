import Link from "next/link";
import type { ReactNode } from "react";

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
      <h1 className="text-2xl font-semibold tracking-tight text-foreground">{title}</h1>
      <p className="text-sm text-muted">{text}</p>
      {children}
      <Link href={linkHref} className="text-sm font-semibold text-primary-text hover:underline">
        {linkLabel}
      </Link>
    </div>
  );
}
